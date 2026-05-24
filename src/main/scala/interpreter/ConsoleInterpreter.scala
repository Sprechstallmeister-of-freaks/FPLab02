package interpreter

import algebra.Console
import infrastructure.IO
import scala.io.StdIn

class ConsoleIO extends Console[IO]:
  def putStrLn(line: String): IO[Unit] = IO(() => println(line))
  def putStr(line: String): IO[Unit]   = IO(() => print(line))
  def getStrLn: IO[String]             = IO(() => StdIn.readLine())
  def flush: IO[Unit]                  = IO(() => System.out.flush())