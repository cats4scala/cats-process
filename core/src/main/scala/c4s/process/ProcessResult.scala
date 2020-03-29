package c4s.process

import cats.effect._
import fs2.Stream

final case class ProcessResult[F[_]](
    exitCode: ExitCode,
    output: Stream[F, Byte],
    error: Stream[F, Byte]
)
