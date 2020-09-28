package c4s.process

import cats.effect._
import fs2.Stream

/**
 * Contains all the outputs returned when executing a [[c4s.process.Process]]
 *
 * @param exitCode Exit code of the execution
 * @param output Stdout of the execution
 * @param error Stderr of the execution
 */
final case class ProcessResult[F[_]](
    exitCode: ExitCode,
    output: Stream[F, Byte],
    error: Stream[F, Byte]
)

final case class ProcessFailure[F[_]](result: ProcessResult[F])
    extends RuntimeException(s"Failed to execute command with exit code ${result.exitCode.code}")
