package pl.enves.ttr.logic
package inner

/**
 * Field 3x3 with fields, ability to set them and rotate.
 */
class BoardQuadrant {
  private[this] val fields = Array.fill[Option[Player.Value]] (3, 3) (None)

}

object BoardQuadrant {
  def apply() = new BoardQuadrant
  def apply(loc: Quadrant.Value) = (loc, new BoardQuadrant)

  // Workaround for an unambiguity
  def named(loc: Quadrant.Value) = apply(loc: Quadrant.Value)
}