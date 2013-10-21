package at.devfest.fnddd.orders.services

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Order domain services boundary
 *
 * Inspired by CQRS and EventSourcing implementation from Daniel Westheide.
 * Implementation is incomplete only for purpose of presentation.
 * For full example implementation please refer to https://github.com/dwestheide/eventhub-flatmap2013
 */
object order {
  /** Layering of imports */
  import akka.actor.{Actor, ActorRef}
  import scalaz.Validation
  import scalaz.syntax.validation._
  import at.devfest.fnddd.orders.domain.order._
  import at.devfest.fnddd.orders.domain.customer._

  /**
   * Repository for Order aggregate root
   */
  class OrderRepository {
    def findOneForId(orderId: OrderId): Validation[String, Order] = ???
    def existOneWithId(orderId: OrderId): Boolean = ???
    def saveOrUpdate(order: Order): Validation[String, Order] = ???
  }

  /**
   * Service for handling Order aggregate root commands
   */
  class OrderService(processor: ActorRef) {
    import scala.concurrent.duration._
    import akka.util.Timeout
    import akka.pattern.ask
    import concurrent.Future

    implicit val timeout = Timeout(5.seconds)

    def execute(command: OrderCommand): Future[Order] = {
      (processor ? command)
        .mapTo[Validation[String, Order]] // Conversion to typed result from Actor
        .map( _.valueOr( cause => throw new RuntimeException(cause)) ) // Conversion from Validation to an Exception within Future
    }
  }

  /** Processor of commands for Order aggregate root */
  class OrderProcessor(orderRepository: OrderRepository) extends Actor {

    /** Command handler will be function, type alias */
    type CommandHandler = OrderCommand => Validation[String, (Order, OrderEvent)] // Expected result from command application is updated order and event what happened

    /** Main service method, executes commands */
    override def receive = {
      case command: OrderCommand => sender ! doCommand(command)
    }

    /** Mapping of commands to its handlers */
    private val doCommand = process { // Each line is command handler, thus fn from OrderCommand to Validation
      case CreateOrder(orderId, customerId, address) =>
        createOrder(orderId, customerId, address)
      case AddProduct(orderId, productId, quantity, unit, price) =>
        updateOrder(orderId, order => order.addProduct(productId, quantity, unit, price)) {
          updatedOrder => ProductAdded(orderId, productId, quantity, unit, price)
        }
      case RemoveProduct(orderId, productId) =>
        updateOrder(orderId, _.removeProduct(productId)) { _ =>
          ProductRemoved(orderId, productId)
        }
      case UpdateQuantity(orderId, productId, quantity) =>
        updateOrder(orderId, _.updateQuantity(productId, quantity)) { _ =>
          QuantityUpdated(orderId, productId, quantity)
        }
      case ChangeShippingAddress(orderId, address) =>
        updateOrder(orderId, _.changeShippingAddress(address)) { _ =>
          ShippingAddressChanged(orderId, address)
        }
    } _ // This is function currying, we transformed method process with 2 params to another function with only 1 param by supplying the first one

    /** Creates an Order */
    private def createOrder(orderId: OrderId, customerId: CustomerId, shippingAddress: Address): Validation[String, (Order, OrderEvent)] =
      if (orderRepository.existOneWithId(orderId)) s"Order with id ${orderId.id} already exists".fail
      else {
        // Event can be created from order, e.g. with generic function
        (Order(orderId, customerId, shippingAddress), OrderCreated(orderId, customerId, shippingAddress)).success
      }

    /** Updates an Order, what to do is high order function */
    private def updateOrder(orderId: OrderId, behavior: Order => Validation[String, Order])(createEvent: Order => OrderEvent): Validation[String, (Order, OrderEvent)] =
      for {
        order <- orderRepository.findOneForId(orderId) // Find order
        updated <- behavior(order) // Apply function on it, normally call of order method
      } yield (updated, createEvent(updated)) // Create domain event by application on updated order

    /** Processing, method encapsulates common steps needed by each service call */
    private def process(commandHandler: CommandHandler)(command: OrderCommand): Validation[String, Order] = {
      val result = commandHandler(command) // Execute command
      result.foreach { case (order, event) => // Result of command handler delivers appropriate event too
        orderRepository.saveOrUpdate(order) // Store changes as side-effect
      }
      result.map ( _._1 )
    }

  }
}
