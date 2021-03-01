package c4s.process

import cats.syntax.all._
import cats.effect._
import fs2.{text, Stream}

package object syntax {

  implicit class ProcessOps[F[_]: Sync](process: Process[F]) {
    import org.typelevel.log4cats.Logger

    /**
     * Wraps the [[c4s.process.Process]] with a `Logger`.
     *
     * @param logger instance to use
     * @return [[c4s.process.Process]] instance with logging enabled
     */
    final def withLogger(logger: Logger[F]): Process[F] = new LoggerProcess(process, logger)
  }

  implicit class StreamBytesOps[F[_]: Sync](stream: Stream[F, Byte]) {

    /**
     * Transform the `Stream` to `String`
     *
     * @return a `String` containing the `Stream` content
     */
    final def asString: F[String] =
      stream
        .through(text.utf8Decode)
        .compile
        .toVector
        .map(_.mkString)

    /**
     * Transform `Stream` to `List[String]`
     *
     * @return a `List[String]` containing each `Stream` line as a String
     */
    final def asLines: F[List[String]] =
      asString
        .map(_.split('\n').toList)
  }

  implicit class ProcessResultOps[F[_]: Sync](result: F[ProcessResult[F]]) {

    /**
     * Transform and return the stdout as a `List[String]`, one per line
     *
     * @return process' stdout as a list of `String`
     */
    final def lines: F[List[String]] = result.flatMap(_.output.asLines)

    /**
     * Transform and return stdout to a `String`
     *
     * @return stdout as `String`
     */
    final def string: F[String] = result.flatMap(_.output.asString)

    /**
     * Raise [[c4s.process.ProcessFailure]] if process exit code is not successful
     *
     * @return tuple with exit code and stdout
     */
    final def strict: F[(ExitCode, Stream[F, Byte])] =
      result.flatMap {
        case r if r.exitCode == ExitCode.Success => Sync[F].pure(r.exitCode -> r.output)
        case r => Sync[F].raiseError(new ProcessFailure(r))
      }
  }
}
