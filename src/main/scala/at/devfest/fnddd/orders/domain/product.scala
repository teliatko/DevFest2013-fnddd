package at.devfest.fnddd.orders.domain

/**
 * Product domain model boundary
 */
object product {
  /** Product ID domain value, implemented as value class */
  case class ProductId(ean: String) extends AnyVal

  /** Product category domain value, implemented as enum */
  object ProductCategory extends Enumeration {
    type ProductCategory = Value
    val Books, Electronics, Food = Value
  }

  /** Product unit domain value, different enum implementation */
  sealed trait ProductUnit
  case object Piece extends ProductUnit
  case object Kg extends ProductUnit
  case object Litre extends ProductUnit

  import ProductCategory._

  /** Product domain entity, aggregate root */
  case class Product(
    identity: ProductId,
    name: String,
    category: ProductCategory,
    price: BigDecimal,
    unit: ProductUnit )
}
