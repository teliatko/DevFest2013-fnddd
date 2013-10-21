package at.devfest.fnddd.orders.domain

/**
 * Order domain model boundary
 */
object order {
  /** Layering of imports */
  import org.joda.time.DateTime
  import scalaz.Validation
  import scalaz.syntax.validation._
  import at.devfest.fnddd.utils._
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

    private def quantityInvariant(quantity: Int): Validation[String, Int] = {
      if (quantity <= 0) s"Quantity must be greater than 0".failure
      else quantity.success
    }

    private def priceInvariant(price: BigDecimal): Validation[String, BigDecimal] = {
      if (price <= 0) s"Price must be greater than 0".failure
      else price.success
    }

    private def findProductById(productId: ProductId): (Option[OrderLine], Vector[OrderLine]) = {
      val (found, rest) = lines.partition( _.productId == productId )
      (found.headOption, rest)
    }

    private def alreadyExists(partitions: (Option[OrderLine], Vector[OrderLine])): Validation[String, (Option[OrderLine], Vector[OrderLine])] = {
      val (found, _) = partitions
      found map { foundLine =>
        s"Product already in order".failure
      } getOrElse partitions.success
    }

    private def notExists(partitions: (Option[OrderLine], Vector[OrderLine])): Validation[String, (Option[OrderLine], Vector[OrderLine])] = {
      val (found, _) = partitions
      found map { foundLine =>
        partitions.success
      } getOrElse {
        s"Product not in order".failure
      }
    }

    private val productAlreadyExists: ProductId => Validation[String, (Option[OrderLine], Vector[OrderLine])] = findProductById _ andThen alreadyExists _
    private val productNotExists: ProductId => Validation[String, (Option[OrderLine], Vector[OrderLine])] = findProductById _ andThen notExists _


    /** Example of aggregate root method */
    def addProduct(
      productId: ProductId, quantity: Int,
      unit: ProductUnit, price: BigDecimal): Validation[String, Order] = {

      for { // First check preconditions, flow works only on SUCCESS (positive scenario only)
        _ <- quantityInvariant(quantity)
        _ <- priceInvariant(price)
        (found, rest) <- productAlreadyExists(productId)
      } yield { // Then update order
        val line = OrderLine(productId, quantity, unit, price)
        copy(lines = rest :+ line)
      }
    }

    /**
     * Another methods implemented similar way as addProduct.
     */

    def removeProduct(productId: ProductId): Validation[String, Order] = {
      // First check preconditions
      productNotExists(productId) map { partitions => // Flow works only on SUCCESS again
        // Then update order
        val (_, rest) = partitions
        copy(lines = rest)
      }
    }

    def updateQuantity(productId: ProductId, quantity: Int): Validation[String, Order] = {
      for { // First check preconditions
        (found, rest) <- productNotExists(productId)
        line = found.get
        resultingQuantity <- quantityInvariant(line.quantity + quantity)
      } yield {
        // Then update order
        copy(lines = rest :+ line.copy(quantity = resultingQuantity))
      }
    }

    def changeShippingAddress(address: Address): Validation[String, Order] = {
      // Update of shipping address
      copy(shipmentAddress = address).success
    }

  }

  /** Commands for Order aggregate root */
  sealed trait OrderCommand

  case class CreateOrder(
    orderId: OrderId,
    customerId: CustomerId,
    shippingAddress: Address) extends OrderCommand

  case class AddProduct(
    orderId: OrderId,
    productId: ProductId,
    quantity: Int,
    unit: ProductUnit,
    price: BigDecimal) extends OrderCommand

  case class RemoveProduct(
    orderId: OrderId,
    productId: ProductId) extends OrderCommand

  case class UpdateQuantity(
    orderId: OrderId,
    productId: ProductId,
    quantity: Int) extends OrderCommand

  case class ChangeShippingAddress(
    orderId: OrderId,
    address: Address) extends OrderCommand


  /** Events for Order aggregate root */
  sealed trait OrderEvent

  case class OrderCreated(
    orderId: OrderId,
    customerId: CustomerId,
    shippingAddress: Address) extends OrderEvent

  case class ProductAdded(
    orderId: OrderId,
    productId: ProductId,
    quantity: Int,
    unit: ProductUnit,
    price: BigDecimal) extends OrderEvent

  case class ProductRemoved(
    orderId: OrderId,
    productId: ProductId) extends OrderEvent

  case class QuantityUpdated(
    orderId: OrderId,
    productId: ProductId,
    quantity: Int) extends OrderEvent

  case class ShippingAddressChanged(
    orderId: OrderId,
    address: Address) extends OrderEvent

}
