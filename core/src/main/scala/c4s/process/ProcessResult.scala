package c4s.process

import cats.effect._
import fs2.Stream

final case class ProcessResult[F[_]](
    exitCode: ExitCode,
    output: Stream[F, Byte],
    error: Stream[F, Byte]
)

final case class ProcessFailure[F[_]](result: ProcessResult[F])
    extends RuntimeException(s"Failed to execute command with exit code ${result.exitCode.code}")
