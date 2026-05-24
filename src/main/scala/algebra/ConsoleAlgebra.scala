package algebra

trait Console[F[_]]:
  def putStrLn(line: String): F[Unit]
  def putStr(line: String): F[Unit]
  def getStrLn: F[String]
  def flush: F[Unit]