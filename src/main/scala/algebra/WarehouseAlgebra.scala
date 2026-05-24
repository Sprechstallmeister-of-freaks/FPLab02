package algebra

import domain.*

/**
  Алгебра складских операций.

  Параметризована абстрактным эффектом F[_] — может быть IO, Future, Id, чем угодно.
  Содержит только сигнатуры методов, никакой реализации.
  Конкретное поведение (хранение в памяти, запись в БД, отправка по сети)
  определяется интерпретатором.
 */
trait Warehouse[F[_]]:

  // Принять поставку товара на склад */
  def receiveShipment(itemName: String, quantity: Int): F[Unit]


   //Зарезервировать товары для заказа.
   //Возвращает Either: Right(()) при успехе, Left(ошибка) при нехватке.

  def reserveItems(order: Order): F[Either[String, Unit]]

  // Перевести заказ в статус "упакован"
  def packOrder(order: Order): F[Unit]

  // Отправить заказ — перенести из собранных в отправленные
  def shipOrder(order: Order): F[Unit]

  // Получить текущие остатки на складе
  def getStock: F[Map[String, Int]]

  // Получить сводку по складу (остатки, собрано, отправлено)
  def getStats: F[String]

  // Получить полное состояние склада
  def getState: F[WarehouseState]