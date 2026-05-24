package interpreter

import algebra.Warehouse
import domain.*
import logic.WarehouseLogic
import infrastructure.IO

class WarehouseIO(initial: WarehouseState) extends Warehouse[IO]:
  private var state: WarehouseState = initial

  def receiveShipment(itemName: String, quantity: Int): IO[Unit] =
    IO { () =>
      state = state.copy(inventory = WarehouseLogic.addStock(itemName, quantity, state.inventory))
    }

  def reserveItems(order: Order): IO[Either[String, Unit]] =
    IO { () =>
      if WarehouseLogic.canAssemble(order, state.inventory) then
        state = state.copy(inventory = WarehouseLogic.reserveStock(order, state.inventory))
        Right(())
      else
        val missing = WarehouseLogic.missingItems(order, state.inventory)
        Left(s"Insufficient stock: ${missing.mkString(", ")}")
    }

  def packOrder(order: Order): IO[Unit] =
    IO { () => state = state.copy(assembled = order :: state.assembled) }

  def shipOrder(order: Order): IO[Unit] =
    IO { () =>
      state = state.copy(
        assembled = state.assembled.filterNot(_ == order),
        shipped = order :: state.shipped
      )
    }

  def getStock: IO[Map[String, Int]] =
    IO { () => state.inventory.stock }

  def getStats: IO[String] =
    IO { () =>
      s"Stock: ${state.inventory.stock.mkString(", ")} | Assembled: ${state.assembled.size} | Shipped: ${state.shipped.size}"
    }

  def getState: IO[WarehouseState] =
    IO { () => state }