package c4s.process

import cats._
import cats.effect._
import cats.syntax.all._
import fs2.{Pipe, Stream}
import java.nio.file.Path

trait FileSystem[F[_]] {
  def path(value: String): F[Path]
  def folderOrFile(value: Path): F[Either[Path, Path]]
  def ls(value: Path): Stream[F, Path]

}

object FileSystem {

  def apply[F[_]](implicit files: FileSystem[F]): FileSystem[F] = files

  def impl[F[_]: Sync]: FileSystem[F] = new JavaFileSystem[F]

  private class JavaFileSystem[F[_]: Sync]() extends FileSystem[F] {
    import java.nio.file._
    import scala.jdk.CollectionConverters._
    def path(value: String): F[Path] = Sync[F].delay(Paths.get(value))

    def folderOrFile(path: Path): F[Either[Path, Path]] = Sync[F]
      .delay(Files.isDirectory(path))
      .ifM(
        path.asRight[Path].pure[F],
        path.asLeft[Path].pure[F]
      )

    def ls(path: Path): Stream[F, Path] = Stream.eval(folderOrFile(path)) >>= (_.fold(
      Stream.emit,
      x => Stream.fromIterator[F](Files.newDirectoryStream(x).iterator().asScala)
    ))
  }

  object syntax {

    def path[F[_]: FileSystem](value: String): F[Path] = FileSystem[F].path(value)
    def folderOrFile[F[_]: FileSystem](path: Path): F[Either[Path, Path]] = FileSystem[F].folderOrFile(path)
    def ls[F[_]: FileSystem](value: Path): Stream[F, Path] = FileSystem[F].ls(value)

    object pipes {

      def folderOrFile[F[_]: FileSystem]: Pipe[F, Path, Either[Path, Path]] =
        _.evalMap(FileSystem.syntax.folderOrFile[F])
      def ls[F[_]: FileSystem]: Pipe[F, Path, Path] = _ >>= (FileSystem.syntax.ls[F])
      def path[F[_]: FileSystem]: Pipe[F, String, Path] = _.evalMap(FileSystem.syntax.path[F])
    }
  }
}

object pipes {
  import c4s.process.Process
  import fs2.text
  import java.nio.charset.StandardCharsets

  def from[F[_], A, B](f: Stream[F, B] => Stream[F, A]): Pipe[F, B, A] = f(_)
  def lift[F[_], A, B](value: B): Pipe[F, A, B] = _ >> Stream.emit(value)
  def select[F[_], A, B](value: B*): Pipe[F, A, B] = _ >> Stream.emits(value.toList)
  def liftF[F[_], A, B](value: F[B]): Pipe[F, A, B] = _.evalMap(_ => value)

  def liftS[F[_], A, B, C](f: Stream[F, A] => F[B], g: B => Stream[F, C]): Pipe[F, A, C] =
    from(x => Stream.eval(f(x)) >>= (g))

  implicit class PipeOps[F[_], A, B](command: Pipe[F, A, B]) {
    def |[C](next: Pipe[F, B, C]): Pipe[F, A, C] = _.through(command).through(next)
  }

  implicit class PipeRunnerOps[F[_]: Sync, A, B](command: Pipe[F, Unit, B]) {
    import c4s.process.syntax._

    def run(f: Pipe[F, B, Byte]): F[String] =
      Stream.emit(()).through(command).through(f).asString
  }

  private def outputFrom[F[_]]: ProcessResult[F] => Stream[F, Byte] = _.output

  def processWithoutInput[F[_]: Process, A](command: String, path: Option[Path]): Pipe[F, A, Byte] =
    liftS(_ => Process[F].run(command, none, path), outputFrom[F])

  def process[F[_]: Process](command: String, path: Option[Path]): Pipe[F, Byte, Byte] =
    liftS(x => Process[F].run(command, x.some, path), outputFrom[F])

  def right[F[_], A, B]: Pipe[F, Either[B, A], A] = _.collect { case Right(x) => x }
  def left[F[_], A, B]: Pipe[F, Either[B, A], B] = _.collect { case Left(x) => x }

  def void[F[_], A]: Pipe[F, A, Unit] = _.map(_ => ())

  def wc[F[_]: Process]: Pipe[F, Byte, Byte] = process("wc -l", None)

  def toBytes[F[_]: Sync]: Pipe[F, Path, Byte] = _ >>= { path =>
    Stream.fromIterator[F](s"$path\n".getBytes(StandardCharsets.UTF_8).iterator)
  }

  def debug[F[_]: Concurrent, A: Show]: Pipe[F, A, A] = _.observeAsync(100) {
    _.evalMap(a => Sync[F].delay(println(a.show)))
  }

  def cat[F[_]: Process, A]: Pipe[F, A, A] = _.map(identity) //TODO Review this

  def pwd[F[_]: Process: FileSystem: Sync, A]: Pipe[F, A, Path] = {
    import c4s.process.syntax._
    import FileSystem.syntax._
    liftF(
      Process[F].run("pwd", None, None).string.map(_.trim()) >>= (path[F])
    )
  } //TODO This is just a way to create complex commands composing the existing ones

  def echo[F[_]: Sync, A](
      value: String
  ): Pipe[F, A, Unit] =
    liftF(Sync[F].delay(println(value)))

  def lines[F[_]: Process]: Pipe[F, Byte, String] = _.through(text.utf8Decode)

  def filter[F[_]: Sync, A](f: A => Boolean): Pipe[F, A, A] = _.filter(f)
}

trait PipeApp extends IOApp {
  import c4s.process.pipes._

  def commands(implicit P: Process[IO]): Pipe[IO, Unit, Byte]

  def run(args: List[String]): IO[ExitCode] = Blocker[IO].use { blocker =>
    implicit val process = Process.impl[IO](blocker)
    (commands.run(cat) >>= (showResult)).as(ExitCode.Success)
  }

  private def showResult(value: String): IO[Unit] = IO {
    println("______")
    println("Result")
    println("______")
    println(s"[${value.show}]")
  }
}

object Commands extends PipeApp {
  import MyCommands._

  implicit val fileSystem: FileSystem[IO] = FileSystem.impl

  override def commands(implicit P: Process[IO]): Pipe[IO, Unit, Byte] = findFiles

}

object MyCommands {
  import c4s.process.pipes._
  import c4s.process.FileSystem.syntax.pipes._

  def lsPwd[F[_]: FileSystem: Process: Concurrent]: Pipe[F, Unit, Byte] =
    pwd | debug | ls | toBytes | cat

  def folders[F[_]: FileSystem]: Pipe[F, Path, Path] = folderOrFile | right
  def files[F[_]: FileSystem]: Pipe[F, Path, Path] = folderOrFile | left

  def findFiles[F[_]: FileSystem: Process: Concurrent]: Pipe[F, Unit, Byte] =
    select[F, Unit, String](".", "/Users/benivilla/dev") |
      path |
      ls |
      folders |
      ls |
      files |
      filter { x =>
        val name = x.getFileName().toString()
        name.contains(".yaml") || name.contains(".lock") || name.contains(".properties")
      } |
      debug |
      toBytes |
      cat |
      wc

  implicit val pathShow: Show[Path] = Show.show(_.toString())

}
