package at.devfest.fnddd.orders.domain

/**
 * Customer domain model boundary
 */
object customer {
  /** Customer ID domain value, implemented as value class */
  case class CustomerId(id: String) extends AnyVal

  /** Customer domain entity, aggregate root */
  case class Customer(
    identity: CustomerId,
    name: String )

  /** Companion object to aggregate root */
  object Customer {
    /** Example of factory method */
    def apply(identity: CustomerId, firstName: String, lastName: String) =
      Customer(identity, s"$firstName $lastName")
  }
}
