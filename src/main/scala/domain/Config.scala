package domain

case class ShippingConfig(
                           tarifs: Map[PackageType, Double],
                           maxWeight: Double,
                           freeShippingThreshold: Double
                         )

case class WarehouseState(
                           inventory: Inventory,
                           assembled: List[Order],
                           shipped: List[Order]
                         )