package c4s.process

import cats.effect.Sync
import cats.implicits._
import fs2.{Stream, text}

final case class ProcessResult[F[_]](
  exitCode: Int,
  output: Stream[F, Byte],
  error: Stream[F, Byte]
)

object ProcessResult {

def mkString[F[_]: Sync](stream: Stream[F, Byte]): F[String] =
  stream.through(text.utf8Decode).compile.toVector.map(_.mkString)
}
