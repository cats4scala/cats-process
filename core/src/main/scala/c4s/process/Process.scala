package c4s.process

import cats.effect._
import cats.implicits._

import java.io.InputStream
import java.nio.file.Path
import fs2.Stream

trait Process[F[_]] {
  def run(command: String, path: Option[Path]): F[ProcessResult[F]]
}

object Process {
  import scala.sys.process.{Process => ScalaProcess, _}
  final def apply[F[_]](implicit process: Process[F]): Process[F] = process

  final def run[F[_]: Process](command: String): F[ProcessResult[F]] = Process[F].run(command, None)

  final def runInPath[F[_]: Process](command: String, path: Path): F[ProcessResult[F]] =
    Process[F].run(command, path.some)

  final def impl[F[_]: Sync: Bracket[?[_], Throwable]: ContextShift](
      blocker: Blocker
  ): Process[F] = new ProcessImpl[F](blocker)

  private[this] final class ProcessImpl[F[_]: Sync: Bracket[?[_], Throwable]: ContextShift](blocker: Blocker)
      extends Process[F] {
    import java.util.concurrent.atomic.AtomicReference
    val atomicReference = Sync[F].delay(new AtomicReference[Stream[F, Byte]])

    final def run(command: String, path: Option[Path]): F[ProcessResult[F]] =
      for {
        outputRef <- atomicReference
        errorRef <- atomicReference
        exitValue <- Bracket[F, Throwable].bracket(Sync[F].delay {
          val p = new ProcessIO(
            _ => (),
            redirectInputStream(outputRef, _),
            redirectInputStream(errorRef, _)
          )
          path.fold(
            ScalaProcess(command).run(p)
          )(path => ScalaProcess(command, path.toFile()).run(p))
        })(p => blocker.blockOn(Sync[F].delay(p.exitValue())))(p => Sync[F].delay(p.destroy()))
        output <- Sync[F].delay(outputRef.get())
        error <- Sync[F].delay(errorRef.get())
      } yield ProcessResult(ExitCode(exitValue), output, error)

    private[this] def redirectInputStream(
        ref: AtomicReference[Stream[F, Byte]],
        is: InputStream
    ): Unit =
      try {
        val queue = scala.collection.mutable.Queue.empty[Byte]
        var n = is.read()
        while(n != -1) {
          queue.enqueue(n.toByte)
          n = is.read()
        }
        ref.set(Stream.fromIterator(queue.iterator))
      } finally {
        is.close()
      }
  }
}
