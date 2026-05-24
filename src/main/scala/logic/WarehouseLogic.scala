package logic

import domain.*

object WarehouseLogic:

  /** Проверить, можно ли собрать заказ из имеющихся остатков */
  def canAssemble(order: Order, inventory: Inventory): Boolean =
    order.items.forall(item => inventory.stock.getOrElse(item.name, 0) > 0)

  /** Выбрать тип упаковки в зависимости от веса */
  def packageType(weight: Double): PackageType =
    if weight <= 2.0 then PackageType.Small
    else if weight <= 10.0 then PackageType.Medium
    else PackageType.Large

  /** Рассчитать стоимость доставки с учётом конфига */
  def shippingCost(weight: Double, totalPrice: Double, config: ShippingConfig): Double =
    if totalPrice >= config.freeShippingThreshold then 0.0
    else config.tarifs(packageType(weight))

  /** Проверить, положена ли бесплатная доставка */
  def freeShipping(totalPrice: Double, config: ShippingConfig): Boolean =
    totalPrice >= config.freeShippingThreshold

  /** Получить список отсутствующих на складе товаров из заказа */
  def missingItems(order: Order, inventory: Inventory): List[String] =
    order.items
      .filter(item => inventory.stock.getOrElse(item.name, 0) <= 0)
      .map(_.name)

  /** Списать товары заказа с остатков — вернуть новый Inventory */
  def reserveStock(order: Order, inventory: Inventory): Inventory =
    val newStock = order.items.foldLeft(inventory.stock)((stock, item) =>
      stock.updated(item.name, stock(item.name) - 1))
    inventory.copy(stock = newStock)

  /** Добавить товар на склад — вернуть новый Inventory */
  def addStock(itemName: String, quantity: Int, inventory: Inventory): Inventory =
    val current = inventory.stock.getOrElse(itemName, 0)
    inventory.copy(stock = inventory.stock.updated(itemName, current + quantity))