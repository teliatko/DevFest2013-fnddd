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
    var created: DateTime = DateTime.now())

}
