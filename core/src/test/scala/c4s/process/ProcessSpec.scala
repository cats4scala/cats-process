package c4s.process

import cats.effect._
import cats.implicits._
import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext

import java.nio.file._

class ProcessSpec extends Specification {
  implicit val executionContext = ExecutionContext.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)

  "Process should be able to" >> {
    import c4s.process.syntax._

    "run commands" >> {
      withProcess(implicit shell => Process.run("ls -la").map(_.exitCode must ===(ExitCode.Success)))
        .unsafeRunSync()
    }

    "strict run commands" >> {
      withProcess(implicit shell => Process.run("ls -la").strict.map(_._1 must ===(ExitCode.Success)))
        .unsafeRunSync()
    }

    "strict run commands when it is failing" >> {
      withProcess(implicit shell => Process.run("ls foo").strict)
        .unsafeRunSync() must throwA[ProcessFailure[IO]]
    }

    "get the output as string and execute commands in different folder" >> {
      withProcess { implicit shell =>
        createTmpDirectory[IO].use { path =>
          for {
            _ <- Process.runInPath("mkdir foo", path)
            output <- Process.runInPath("ls", path).string
            result <- IO {
              output must contain(s"foo")
            }
          } yield result
        }
      }.unsafeRunSync()
    }

    "get lines and execute commands in different folder" >> {
      withProcess { implicit shell =>
        createTmpDirectory[IO].use { path =>
          for {
            _ <- Process.runInPath("mkdir foo", path)
            _ <- Process.runInPath("mkdir bar", path)
            output <- Process.runInPath("ls", path).lines
            result <- IO {
              output must beEqualTo(List("bar", "foo"))
            }
          } yield result
        }
      }.unsafeRunSync()
    }

    "run command reading a stream from another command" >> {
      withProcess{ implicit shell =>
        for {
          result <- Process.run("ls -las")
          resultStream <- Process.run("wc", result.output)
        } yield resultStream.exitCode == (ExitCode.Success)
      }.unsafeRunSync()
    }

  }

  def withProcess[R](f: Process[IO] => IO[R]): IO[R] =
    Blocker[IO]
      .use(x => f(Process.impl[IO](x)))

  def createTmpDirectory[F[_]: Sync]: Resource[F, Path] =
    Resource.make(Sync[F].delay(Files.createTempDirectory("tmp")))(path =>
      Sync[F].delay(scala.reflect.io.Directory(path.toFile()).deleteRecursively()).void
    )

}
