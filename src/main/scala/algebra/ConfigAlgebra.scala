package algebra

import domain.ShippingConfig

trait Config[F[_]]:
  def load: F[ShippingConfig]