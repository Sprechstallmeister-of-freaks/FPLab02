package application

import domain.*
import logic.WarehouseLogic
import algebra.*
import infrastructure.Monad
import infrastructure.Monad.*

class OrderProcessor[F[_]: Monad](
                                   console: Console[F],
                                   config: Config[F],
                                   logging: Logging[F],
                                   warehouse: Warehouse[F]
                                 ):

  // Основной сценарий: отчёт по доставке + складские операции
  def processWithWarehouse(order: Order): F[String] =
    for
      // Загрузка конфига и расчёт параметров доставки
      cfg    <- config.load
      pkg     = WarehouseLogic.packageType(order.totalWeight)
      cost    = WarehouseLogic.shippingCost(order.totalWeight, order.totalPrice, cfg)
      isFree  = WarehouseLogic.freeShipping(order.totalPrice, cfg)

      // Логирование параметров заказа
      _ <- logging.log(s"Items: ${order.items.map(i => s"${i.name} (${i.weight}kg, ${i.price}RUB)").mkString("; ")}\n")
      _ <- logging.log(s"Calculated weight: ${order.totalWeight} kg\n")
      _ <- logging.log(s"Package selected: $pkg\n")
      _ <- logging.log(if isFree then "Shipping: FREE\n" else s"Shipping cost: $cost RUB\n")
      _ <- logging.log(s"Free shipping: ${if isFree then "YES" else "NO"} (threshold: ${cfg.freeShippingThreshold})\n")

      // Формирование и вывод отчёта
      report = s"""
                  |=== ORDER REPORT ===
                  |Items: ${order.items.map(i => s"${i.name} (${i.weight}kg, ${i.price}RUB)").mkString("; ")}
                  |Weight: ${order.totalWeight} kg | Package: $pkg
                  |${if isFree then "Shipping: FREE" else s"Shipping cost: $cost RUB"}
                  |Free shipping: ${if isFree then "YES" else "NO"}
                  |==========================
        """.stripMargin

      _ <- console.putStrLn(report)
      _ <- console.putStrLn("\n=== WAREHOUSE ===")

      // Приёмка поставки и проверка остатков
      _ <- warehouse.receiveShipment("Laptop", 3)
      _ <- logging.log("Shipment received: Laptop +3\n")

      stock   <- warehouse.getStock
      _       <- logging.log(s"Current stock: ${stock.mkString(", ")}\n")

      // Попытка резервирования (ветвление по результату)
      reserve <- warehouse.reserveItems(order)
      result  <- reserve match
        case Left(err) =>
          logging.log(s"FAILED: $err\n") *>
            console.putStrLn(s"\n=== ORDER REJECTED ===\n$err\n")
              .as(s"REJECTED: $err")
        case Right(_) =>
          for
            _ <- logging.log(s"Reserved: ${order.items.map(i => s"${i.name} -1").mkString(", ")}\n")
            _ <- warehouse.packOrder(order)
            _ <- logging.log(s"Order packed: ${order.items.map(_.name).mkString(", ")}\n")
            _ <- warehouse.shipOrder(order)
            _ <- logging.log(s"Order shipped: ${order.items.map(_.name).mkString(", ")}\n")
            s <- warehouse.getStats
            _ <- console.putStrLn(s"Order packed & shipped\n$s\n=== DONE ===")
          yield s"OK: $s"
    yield result