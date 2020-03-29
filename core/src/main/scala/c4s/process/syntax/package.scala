package c4s.process

import cats.implicits._
import cats.effect._
import fs2.{text, Stream}

package object syntax {

  implicit class StreamBytesOps[F[_]: Sync](stream: Stream[F, Byte]) {

    def asString: F[String] =
      stream
        .through(text.utf8Decode)
        .compile
        .toVector
        .map(_.mkString)

    def asLines: F[List[String]] =
      asString
        .map(_.split('\n').toList)
  }

  implicit class ProcessResultOps[F[_]: Sync](result: F[ProcessResult[F]]) {
    def lines: F[List[String]] = result.flatMap(_.output.asLines)
    def string: F[String] = result.flatMap(_.output.asString)

    def strict: F[(ExitCode, Stream[F, Byte])] = result.flatMap {
      case r if r.exitCode == ExitCode.Success => Sync[F].pure(r.exitCode -> r.output)
      case r => Sync[F].raiseError(new ProcessFailure(r))
    }
  }
}
