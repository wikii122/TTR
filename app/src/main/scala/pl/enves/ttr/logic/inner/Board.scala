package pl.enves.ttr.logic
package inner

/**
 * Manages fields states.
 */
private[logic] class Board {
  private[this] var _version = 0
  private[this] val quadrants = createQuadrants.toMap

  def version: Int = _version

  def move(x: Int, y: Int, player: Player.Value): Boolean = ???

  def rotate(quadrant: Quadrant.Value, rotation: Rotation.Value): Boolean = ???

  private def createQuadrants = Quadrant.values.toList map BoardQuadrant.named
}
