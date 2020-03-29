package c4s.process

import io.chrisdavenport.log4cats.testing.TestingLogger
import io.chrisdavenport.log4cats.testing.TestingLogger._
import cats.effect._
import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext

class LoggerProcessSpec extends Specification {
  implicit val executionContext = ExecutionContext.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)

  "Process should be able to" >> {
    import c4s.process.syntax._

    "to log what we are doing and still getting the command when it is succeeded" >> {
      withProcess { (shell, logger) =>
        for {
          output <- Process.run("ls -la")(shell).string
          log <- logger.logged
        } yield log.collect { case INFO(message, _) => message } must contain(output)
      }.unsafeRunSync()
    }

    "to log what we are doing and still getting the command when it fails" >> {
      withProcess { (shell, logger) =>
        for {
          result <- Process.run("ls foo")(shell)
          error <- result.error.asString
          log <- logger.logged
        } yield log.collect { case ERROR(message, _) => message } must contain(error)
      }.unsafeRunSync()
    }
  }

  def withProcess[R](f: (Process[IO], TestingLogger[IO]) => IO[R]): IO[R] = {
    val logger = TestingLogger.impl[IO]()
    Blocker[IO]
      .use(x => f(Process.impl[IO](x).withLogger(logger), logger))
  }

}
