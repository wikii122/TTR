package pl.enves.ttr.logic
package inner

import pl.enves.ttr.utils.Logging

/**
 * Manages fields states.
 */
private[logic] class Board extends Logging {
  private[this] var _version = 0
  private[this] val quadrants = createQuadrants.toMap
  private[this] var _winner: Option[Player.Value] = None

  def version: Int = _version

  def move(x: Int, y: Int, player: Player.Value): Boolean = {
    // TODO automate this
    val quad = if (y < Quadrant.size) {
      if (x < Quadrant.size) Quadrant.first
      else Quadrant.second
    } else {
      if (x < Quadrant.size) Quadrant.third
      else Quadrant.fourth
    }

    log(s"Move of $player at ($x, $y) in $quad")
    quadrants(quad).move(x, y, player)

    return checkVictory()
  }

  def rotate(quadrant: Quadrant.Value, rotation: Rotation.Value): Boolean = {
    log(s"Rotation from ${Game.player} for $quadrant by $rotation")
    quadrants(quadrant).rotate(rotation)

    return checkVictory()
  }

  def finished = _winner

  def lines: Seq[Seq[Option[Player.Value]]] = (0 to 5) map {
    i => if (i < Quadrant.size)
      quadrants(Quadrant.first).line(i) ++ quadrants(Quadrant.second).line(i)
    else
      quadrants(Quadrant.third).line(i % Quadrant.size) ++ quadrants(Quadrant.fourth).line(i % Quadrant.size)
  }

  private def createQuadrants = Quadrant.values.toList map BoardQuadrant.named

  // TODO!!!!
  private def checkVictory(): Boolean = false
}
