package c4s.process

import cats.effect._
import cats.implicits._
import fs2.{Stream, text}
import io.chrisdavenport.log4cats.Logger

import java.io.InputStream
import java.nio.file.Path


trait Process[F[_]] {
  def run(command: String): F[ProcessResult[F]]
  def run(command: String, path: Path): F[ProcessResult[F]]
}

object Process {
  import scala.sys.process.{Process => ScalaProcess, _}
  def apply[F[_]](implicit process: Process[F]): Process[F] = process

  implicit class ProcessOps[F[_]: Sync](process: Process[F]) {

    def withLogger(logger: Logger[F]): Process[F] = new Process[F] {

      def run(command: String): F[ProcessResult[F]] =
        for {
          result <- process.run(command)
          newResult <- logProcessResult(None, command, result)
        } yield newResult

      def run(command: String, path: Path): F[ProcessResult[F]] =
        for {
          result <- process.run(command, path)
          newResult <- logProcessResult(path.some, command, result)
        } yield newResult

      def logProcessResult(path: Option[Path], command: String, result: ProcessResult[F]): F[ProcessResult[F]] =
        for {
          _ <- logger.info(s"[${result.exitCode}] ${path.fold("")(x => s"$x/")}$command")
          output <- ProcessResult.mkString(result.output)
          _ <- if (output.nonEmpty)
            logger.info(output)
          else
            Sync[F].pure(())
          error <- ProcessResult.mkString(result.error)
          _ <- if (error.nonEmpty)
            logger.error(error)
          else
            Sync[F].pure(())
        } yield result.copy(
          output = Stream(output).through(text.utf8Encode),
          error = Stream(error).through(text.utf8Encode)
        )
    }
  }

  def impl[F[_]: Sync: Bracket[?[_], Throwable]: ContextShift](
      blocker: Blocker
  ): Process[F] =
    new Process[F] {
      import java.util.concurrent.atomic.AtomicReference
      val atomicReference = Sync[F].delay(new AtomicReference[Stream[F, Byte]])

      def run(command: String): F[ProcessResult[F]] = run(command, None)
      def run(command: String, path: Path): F[ProcessResult[F]] = run(command, path.some)

      def run(command: String, path: Option[Path]): F[ProcessResult[F]] =
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
          })(
            p => blocker.blockOn(Sync[F].delay(p.exitValue()))
          )(p => Sync[F].delay(p.destroy()))
          output <- Sync[F].delay(outputRef.get())
          error <- Sync[F].delay(errorRef.get())
        } yield ProcessResult(exitValue, output, error)

      private[this] def redirectInputStream(
          ref: AtomicReference[Stream[F, Byte]],
          is: InputStream
      ): Unit =
        try {
          import scala.collection.immutable.LazyList
          val output = LazyList
            .continually(is.read)
            .takeWhile(_ != -1)
            .map(_.toByte)
            .iterator
            .toArray
          ref.set(Stream.fromIterator(output.iterator))
        } finally {
          is.close()
        }
    }
}
