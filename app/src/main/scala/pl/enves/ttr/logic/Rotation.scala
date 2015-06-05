package pl.enves.ttr.logic

/**
 * Used to represent rotation move information.
 *
 * Invalidates after any change in the board layout, such as rotation.
 * @param board in form of
 *              1 2
 *              3 4
 * @param r in rotation enumerator counted in degrees clockwise.
 */
case class Rotation(board: Quadrant.Value, r: Rotation.Value) extends Move

object Rotation extends Enumeration {
  val r90, r180, r270 = Value
}

object Quadrant extends Enumeration {
  val first, second, third, fourth = Value
}