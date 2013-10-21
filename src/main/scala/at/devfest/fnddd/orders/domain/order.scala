package at.devfest.fnddd.orders.domain

/**
 * Order domain model boundary
 */
object order {
  /** Layering of imports */
  import org.joda.time.DateTime
  import at.devfest.fnddd.orders.domain.customer._
  import at.devfest.fnddd.orders.domain.product._

  import scala.collection.mutable.ArrayBuffer

  /** Order ID domain value */
  class OrderId(var id: String)

  /** Address domain value */
  class Address(
    var street: String,
    var zipCode: String,
    var city: String,
    var country: String)

  /** Order line domain value */
  class OrderLine(
    var productId: ProductId,
    var quantity: Int,
    var unit: ProductUnit,
    var price: BigDecimal )

  /** Order domain entity, aggregate root */
  class Order(
    var id: OrderId,
    var customerId: CustomerId,
    var shipmentAddress: Address,
    var lines: ArrayBuffer[OrderLine] = ArrayBuffer.empty,
    var created: DateTime = DateTime.now()) {

    /** Example of aggregate root method */
    def addProduct(
      productId: ProductId, quantity: Int,
      unit: ProductUnit, price: BigDecimal) {
      // Just checking some invariants
      require(quantity > 0, s"Quantity must be greater than 0")
      require(price > 0, s"Price must be greater than 0")

      // Lookup if product is not already in order
      val (existing, rest) = lines.partition( _.productId == productId )
      require(existing.isEmpty, s"$productId not in order")

      // Add product to order
      val line = new OrderLine(productId, quantity, unit, price)
      lines += line
    }

    /**
     * Another methods for manipulating order. Left unimplemented,
     * similar approach like in addProduct.
     */

    def removeProduct(productId: ProductId) { ??? }

    def updateQuantity(productId: ProductId, quantity: Int) { ??? }

    def changeShippingAddress(address: Address) { ??? }

  }

}
