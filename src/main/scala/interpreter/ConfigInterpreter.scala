package interpreter

import algebra.Config
import domain.{ShippingConfig, PackageType}
import infrastructure.IO
import scala.io.Source

// Чтение конфигурации доставки из ресурсного файла
class ConfigIO extends Config[IO]:
  def load: IO[ShippingConfig] =
    IO { () =>
      val src = Source.fromResource("shipping.conf")
      try
        val map = src.getLines().map(_.trim)
          .filter(l => l.nonEmpty && l.contains("="))
          .map { line =>
            val p = line.split("=").map(_.trim)
            p(0) -> p(1).toDouble
          }.toMap
        ShippingConfig(
          tarifs = Map(
            PackageType.Small  -> map.getOrElse("small-tariff", 100.0),
            PackageType.Medium -> map.getOrElse("medium-tariff", 250.0),
            PackageType.Large  -> map.getOrElse("large-tariff", 500.0)
          ),
          maxWeight = map.getOrElse("max-weight", 20.0),
          freeShippingThreshold = map.getOrElse("free-shipping-threshold", 1000.0)
        )
      finally src.close()
    }