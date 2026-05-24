package infrastructure

trait Monad[F[_]]:
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

object Monad:
  extension [F[_], A](fa: F[A])(using m: Monad[F])
    def map[B](f: A => B): F[B] = m.flatMap(fa)(a => m.pure(f(a)))
    def flatMap[B](f: A => F[B]): F[B] = m.flatMap(fa)(f)
    def *>[B](fb: F[B]): F[B] = m.flatMap(fa)(_ => fb)
    def as[B](b: B): F[B] = m.flatMap(fa)(_ => m.pure(b))