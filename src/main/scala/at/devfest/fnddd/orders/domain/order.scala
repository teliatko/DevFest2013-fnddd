package at.devfest.fnddd.orders.domain

/**
 * Order domain model boundary
 */
object order {
  /** Layering of imports */
  import org.joda.time.DateTime

  import scalaz.Validation
  import scalaz.syntax.validation._

  import at.devfest.fnddd.orders.domain.customer._
  import at.devfest.fnddd.orders.domain.product._

  /** Order ID domain value, implemented as value class */
  case class OrderId(id: String) extends AnyVal

  /** Address domain value */
  case class Address(
    street: String,
    zipCode: String,
    city: String,
    country: String)

  /** Order line domain value */
  case class OrderLine(
    productId: ProductId,
    quantity: Int,
    unit: ProductUnit,
    price: BigDecimal )

  /** Order domain entity, aggregate root */
  case class Order(
    id: OrderId,
    customerId: CustomerId,
    shipmentAddress: Address,
    lines: Vector[OrderLine] = Vector.empty,
    created: DateTime = DateTime.now()) {

    /** Example of aggregate root method */
    def addProduct(
      productId: ProductId, quantity: Int,
      unit: ProductUnit, price: BigDecimal): Validation[String, Order] = {
      // Just checking some invariants
      if (quantity <= 0) s"Quantity must be greater than 0".failure
      else if (price <= 0) s"Price must be greater than 0".failure
      else {
        // Lookup if product is not in order
        val (existing, rest) = lines.partition( _.productId == productId )
        if (existing.nonEmpty) s"$productId already in order".failure
        else {
          // Add product to order
          val line = OrderLine(productId, quantity, unit, price)
          copy(lines = rest :+ line).success
        }
      }
    }

    /**
     * Another methods implemented similar way as addProduct.
     * Better, but still not vary functional
     */

    def removeProduct(productId: ProductId): Validation[String, Order] = {
      // Lookup if product is in order
      val (remove, rest) = lines.partition( _.productId == productId )
      if (remove.isEmpty) s"$productId not in order".failure
      else // Remove product from order
        copy(lines = rest).success
    }

    def updateQuantity(productId: ProductId, quantity: Int): Validation[String, Order] = {
      // Lookup if product is not in order
      val (existing, rest) = lines.partition( _.productId == productId )
      if (existing.isEmpty) s"$productId not in order".failure
      else {
        val line = existing.head
        val resultingQuantity = line.quantity + quantity
        if (resultingQuantity <= 0) s"Resulting quantity is negative or 0".failure

        // Update of product and order
        copy(lines = rest :+ line.copy(quantity = resultingQuantity)).success
      }
    }

    def changeShippingAddress(address: Address): Validation[String, Order] = {
      // Update of shipping address
      copy(shipmentAddress = address).success
    }

  }

}
