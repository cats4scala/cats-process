package c4s.process

import io.chrisdavenport.log4cats.testing.TestingLogger
import io.chrisdavenport.log4cats.testing.TestingLogger._
import cats.effect._
import munit.CatsEffectSuite

class LoggerProcessSpec extends CatsEffectSuite {
  import c4s.process.syntax._

  test("it should log what we are doing and still getting the command when it is succeeded") {
    withProcess.use { case (shell, logger) =>
      for {
        output <- Process.run("ls -la")(shell).string
        log <- logger.logged
      } yield assert(
        log
          .collect { case INFO(message, _) => message }
          .contains(output)
      )
    }
  }

  test("it should log what we are doing and still getting the command when it fails") {
    withProcess.use { case (shell, logger) =>
      for {
        result <- Process.run("ls foo")(shell)
        error <- result.error.asString
        log <- logger.logged
      } yield assert(
        log
          .collect { case ERROR(message, _) => message }
          .contains(error)
      )
    }
  }

  def withProcess: Resource[IO, (Process[IO], TestingLogger[IO])] = {
    val logger = TestingLogger.impl[IO]()
    Blocker[IO]
      .map(x => (Process.impl[IO](x).withLogger(logger), logger))
  }

}
