package at.devfest.fnddd.orders.domain

/**
 * Order domain model boundary
 */
object order {
  /** Layering of imports */
  import org.joda.time.DateTime
  import at.devfest.fnddd.orders.domain.customer._
  import at.devfest.fnddd.orders.domain.product._

  /** Order ID domain value, implemented as value class */
  case class OrderId(id: String) extends AnyVal

  /** Address domain value */
  class Address(
    val street: String,
    val zipCode: String,
    val city: String,
    val country: String)

  /** Order line domain value */
  class OrderLine(
    val productId: ProductId,
    val quantity: Int,
    val unit: ProductUnit,
    val price: BigDecimal )

  /** Order domain entity, aggregate root */
  class Order(
    val id: OrderId,
    val customerId: CustomerId,
    val shipmentAddress: Address,
    val lines: Vector[OrderLine] = Vector.empty,
    val created: DateTime = DateTime.now()) {

    /** Example of aggregate root method */
    def addProduct(
      productId: ProductId, quantity: Int,
      unit: ProductUnit, price: BigDecimal): Order = {
      // Just checking some invariants
      require(quantity > 0, s"Quantity must be greater than 0")
      require(price > 0, s"Price must be greater than 0")

      // Lookup if product is not in order
      val (existing, rest) = lines.partition( _.productId == productId )
      require(existing.isEmpty, s"$productId not in order")

      // Add product to order
      val line = new OrderLine(productId, quantity, unit, price)
      new Order(this.id, this.customerId, this.shipmentAddress, this.lines :+ line, this.created)
    }

   /**
    * Another methods for manipulating order. Left unimplemented,
    * similar approach like in addProduct.
    */

    def removeProduct(productId: ProductId): Order = { ??? }

    def updateQuantity(productId: ProductId, quantity: Int): Order = { ??? }

    def changeShippingAddress(address: Address): Order = { ??? }


  }

}
