import domain.*
import infrastructure.IO
import infrastructure.Monad.*
import algebra.*
import interpreter.*
import application.OrderProcessor
import scala.io.StdIn


@main def main(): Unit =

  // Инициализация интерпретаторов, все эффекты будут через IO
  val console   = ConsoleIO()
  val config    = ConfigIO()
  val logging   = LoggingIO()
  val warehouse = WarehouseIO(
    WarehouseState(
      Inventory(Map("Scala book" -> 5, "Gel pen" -> 10, "Laptop" -> 0)),
      List.empty,
      List.empty
    )
  )

  //  внедрение зависимостей
  val processor = OrderProcessor(console, config, logging, warehouse)

  // Описание программы как цепочки IO-действий
  val program = for
    _ <- console.putStrLn("=" * 50)
    _ <- console.putStrLn("WAREHOUSE ORDER PROCESSING SYSTEM")
    _ <- console.putStrLn("=" * 50)
    _ <- console.putStrLn("\nEnter items (name,weight,price). Type 'done' to finish.\n")

    items <- readItems(console, List.empty)

    totalWeight = items.map(_.weight).sum
    totalPrice  = items.map(_.price).sum
    order = Order(items, totalWeight, totalPrice)

    _ <- console.putStrLn(s"\nOrder: ${items.map(_.name).mkString(", ")}")
    _ <- console.putStrLn(s"Weight: $totalWeight kg | Price: $totalPrice RUB")
    _ <- console.putStrLn("\nPress ENTER to process...")
    _ <- console.getStrLn

    result <- processor.processWithWarehouse(order)
    _      <- console.putStrLn(result)

    // Вывод накопленных логов
    logs   <- logging.getLogs
    _      <- console.putStrLn("\n=== LOGS ===")
    _      <- IO.pure(logs.foreach(l => print(l)))

  yield ()

  // Запуск всех эффектов (точка выполнения)
  program.unsafeRun()

// Рекурсивное чтение товаров без изменяемого состояния
def readItems(console: Console[IO], acc: List[Item]): IO[List[Item]] =
  for
    _     <- console.putStr("Item: ")
    _     <- console.flush
    input <- console.getStrLn
    result <- input match
      case null | ""                 => IO.pure(acc.reverse)
      case s if s.toLowerCase == "done" => IO.pure(acc.reverse)
      case s =>
        val parts = s.split(",").map(_.trim)
        if parts.length == 3 && parts(0).nonEmpty then
          try
            val item = Item(parts(0), parts(1).toDouble, parts(2).toDouble)
            console.putStrLn(s"  Added: ${parts(0)}").flatMap(_ =>
              readItems(console, item :: acc)
            )
          catch
            case _: NumberFormatException =>
              console.putStrLn(s"  Invalid numbers").flatMap(_ => readItems(console, acc))
        else
          console.putStrLn(s"  Invalid format").flatMap(_ => readItems(console, acc))
  yield result