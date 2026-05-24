package domain

case class Item(name: String, weight: Double, price: Double)
case class Order(items: List[Item], totalWeight: Double, totalPrice: Double)
case class Inventory(stock: Map[String, Int])

enum PackageType:
  case Small, Medium, Large