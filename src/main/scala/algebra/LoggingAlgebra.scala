package algebra

trait Logging[F[_]]:
  def log(msg: String): F[Unit]
  def getLogs: F[List[String]]