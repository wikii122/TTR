package pl.enves.ttr.logic
package inner

/**
 * Manages fields states.
 */
private[logic] class Board {
  private[this] var _version = 0
  private[this] val quadrants = createQuadrants.toMap
  private[this] var _winner: Option[Player.Value] = None

  def version: Int = _version

  def move(x: Int, y: Int, player: Player.Value): Boolean = {
    // TODO automate this
    val quad = if (y < Quadrant.size) {
      if (x < Quadrant.size) quadrants(Quadrant.first)
      else quadrants(Quadrant.second)
    } else {
      if (x < Quadrant.size) quadrants(Quadrant.third)
      else quadrants(Quadrant.fourth)
    }

    quad.move(x, y, player)

    return checkVictory()
  }

  def rotate(quadrant: Quadrant.Value, rotation: Rotation.Value): Boolean = {
    quadrants(quadrant).rotate(rotation)

    return checkVictory()
  }

  def finished = _winner

  private def createQuadrants = Quadrant.values.toList map BoardQuadrant.named

  // TODO!!!!
  private def checkVictory(): Boolean = false
}
