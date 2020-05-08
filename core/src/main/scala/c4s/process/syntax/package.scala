package c4s.process

import cats.implicits._
import cats.effect._
import fs2.{text, Stream}

package object syntax {

  implicit class ProcessOps[F[_]: Sync](process: Process[F]) {
    import io.chrisdavenport.log4cats.Logger
    final def withLogger(logger: Logger[F]): Process[F] = new LoggerProcess(process, logger)
  }

  implicit class StreamBytesOps[F[_]: Sync](stream: Stream[F, Byte]) {

    final def asString: F[String] =
      stream
        .through(text.utf8Decode)
        .compile
        .toVector
        .map(_.mkString)

    final def asLines: F[List[String]] =
      asString
        .map(_.split('\n').toList)
  }

  implicit class ProcessResultOps[F[_]: Sync](result: F[ProcessResult[F]]) {
    final def lines: F[List[String]] = result.flatMap(_.output.asLines)
    final def string: F[String] = result.flatMap(_.output.asString)

    final def strict: F[(ExitCode, Stream[F, Byte])] =
      result.flatMap {
        case r if r.exitCode == ExitCode.Success => Sync[F].pure(r.exitCode -> r.output)
        case r => Sync[F].raiseError(new ProcessFailure(r))
      }
  }
}
