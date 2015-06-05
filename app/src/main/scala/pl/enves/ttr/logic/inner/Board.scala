package pl.enves.ttr.logic
package inner

/**
 * Keeps fields states.
 */
private[logic] class Board {
  private[this] var _version = 0
  private[this] val quadrants = (Quadrant.values map ((_, new BoardQuadrant))).toMap

  def version: Int = _version

  def move(x: Int, y: Int, player: Player.Value): Boolean = ???

  def rotate(quadrant: Quadrant.Value, rotation: Rotation.Value): Boolean = ???
}
