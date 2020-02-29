package c4s.process

import org.specs2._
import cats.effect._

class MainSpec extends mutable.Specification {

  "Main" should {
    "run a println" in {
      Main.run(List.empty[String]).unsafeRunSync.should_===(ExitCode.Success)
    }
  }

}