package c4s.process

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.testing.TestingLogger
import io.chrisdavenport.log4cats.testing.TestingLogger._ 
import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext

import java.nio.file._

class ProcessSpec extends Specification {
  implicit val executionContext = ExecutionContext.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)

  "Process should be able to" >> {

    "run commands" >> {
      withProcess {
        _.run("ls -la").map(_.exitCode must ===(0))
      }.unsafeRunSync()
    }

    "get the outputs and execute commands in different folder" >> {
      withProcess { shell =>
        createTmpDirectory[IO].use { path =>
          for {
            _ <- shell.run("mkdir foo", path)
            result <- shell.run("ls", path)
            output <- ProcessResult.mkString(result.output)
            result <- IO {
              result.exitCode must ===(0)
              output must contain(s"foo")
            }
          } yield result
        }
      }.unsafeRunSync()
    }

    "to log what we are doing and still getting the command when it is succeeded" >> {
      withProcess { shell =>
        val logger = TestingLogger.impl[IO]()
        for {
          result <- shell.withLogger(logger).run("ls -la")
          output <- ProcessResult.mkString(result.output)
          log <- logger.logged
        } yield log.collect{ case INFO(message, _) => message } must contain(output)
      }.unsafeRunSync()
    }

    "to log what we are doing and still getting the command when it fails" >> {
      withProcess { shell =>
        val logger = TestingLogger.impl[IO]()
        for {
          result <- shell
            .withLogger(logger)
            .run("ls foo")
          error <- ProcessResult.mkString(result.error)
          log <- logger.logged
        } yield log.collect{case ERROR(message, _) => message} must contain (error)
      }.unsafeRunSync()
    }
  }

  def withProcess[R](f: Process[IO] => IO[R]): IO[R] =
    Blocker[IO]
      .use(x => f(Process.impl[IO](x)))

  def createTmpDirectory[F[_]: Sync]: Resource[F, Path] =
    Resource.make(Sync[F].delay(Files.createTempDirectory("tmp")))(
      path => Sync[F].delay(scala.reflect.io.Directory(path.toFile()).deleteRecursively()).void
    )

}
