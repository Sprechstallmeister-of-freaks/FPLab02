package interpreter

import algebra.Logging
import infrastructure.IO

class LoggingIO extends Logging[IO]:
  private var logs: List[String] = List.empty

  def log(msg: String): IO[Unit] = IO { () => logs = msg :: logs }

  def getLogs: IO[List[String]] = IO { () => logs.reverse }