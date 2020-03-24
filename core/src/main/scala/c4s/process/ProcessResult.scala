package c4s.process

import cats.effect._
import cats.implicits._
import fs2.{text, Stream}

final case class ProcessResult[F[_]](
  exitCode: ExitCode,
  output: Stream[F, Byte],
  error: Stream[F, Byte]
)

object ProcessResult {

  def mkString[F[_]: Sync](stream: Stream[F, Byte]): F[String] =
    stream.through(text.utf8Decode).compile.toVector.map(_.mkString)
}
