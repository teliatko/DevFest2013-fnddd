package at.devfest.fnddd

import scalaz.Monoid

/**
 * Utility classes and objects, mainly for Scalaz
 */
package object utils {
  /** String monoid, maybe this is somewhere in scalaz, but I didn't find it */
  implicit lazy val strMonoid: Monoid[String] = new Monoid[String] {
    def zero = ""
    def append(x: String, y: => String) = x + y
  }
}

