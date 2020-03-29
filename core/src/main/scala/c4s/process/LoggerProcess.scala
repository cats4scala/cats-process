package c4s.process

import cats.implicits._
import cats.effect._
import io.chrisdavenport.log4cats.Logger
import c4s.process.syntax._
import java.nio.file.Path
import fs2.{text, Stream}

private[process] final class LoggerProcess[F[_]: Sync](process: Process[F], logger: Logger[F]) extends Process[F] {

  def run(command: String, path: Option[Path]): F[ProcessResult[F]] =
    for {
      result <- process.run(command, path)
      newResult <- logProcessResult(path, command, result)
    } yield newResult

  private def logProcessResult(path: Option[Path], command: String, result: ProcessResult[F]): F[ProcessResult[F]] =
    for {
      _ <- logger.info(s"[${result.exitCode}] ${path.fold("")(x => s"$x/")}$command")
      output <- result.output.asString
      _ <- if (output.nonEmpty)
        logger.info(output)
      else
        Sync[F].pure(())
      error <- result.error.asString
      _ <- if (error.nonEmpty)
        logger.error(error)
      else
        Sync[F].pure(())
    } yield result.copy(
      output = Stream(output).through(text.utf8Encode),
      error = Stream(error).through(text.utf8Encode)
    )
}
