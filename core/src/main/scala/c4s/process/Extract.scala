package c4s.process

import cats.effect.IO

trait Extract[F[_]] {
  def extract[A](fa: F[A]): A
}

object Extract {

  final def apply[F[_]](implicit extract: Extract[F]): Extract[F] = extract

  implicit val extractIO: Extract[IO] = new Extract[IO] {
    override def extract[A](fa: IO[A]): A = fa.unsafeRunSync()
  }
}
