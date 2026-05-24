package infrastructure

case class IO[A](unsafeRun: () => A)

object IO:
  def pure[A](a: => A): IO[A] = IO(() => a)

  given Monad[IO] with
    def pure[A](a: A): IO[A] = IO.pure(a)
    def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] =
      IO(() => f(fa.unsafeRun()).unsafeRun())