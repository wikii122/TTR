package pl.enves.ttr.logic
package inner

import pl.enves.ttr.utils.Logging

/**
 * Manages fields states.
 */
private[logic] class Board extends Logging {
  private[this] val quadrants = createQuadrants.toMap
  private[this] var _version = 0
  private[this] var freeFields = 36
  private[this] var _winner: Option[Player.Value] = None
  private[this] var _combination: List[(Int, Int)] = Nil

  def version: Int = _version

  def move(x: Int, y: Int, player: Player.Value): Boolean = {
    if ((freeFields == 0) && _winner.isEmpty) throw new GameDrawn

    // TODO automate this
    val quad = if (y < Quadrant.size) {
      if (x < Quadrant.size) Quadrant.first
      else Quadrant.second
    } else {
      if (x < Quadrant.size) Quadrant.third
      else Quadrant.fourth
    }

    log(s"Move of $player at ($x, $y) in $quad quadrant")
    quadrants(quad).move(x, y, player)

    _version += 1
    freeFields -= 1
    quadrants.tick()

    return checkVictory()
  }

  def rotate(quadrant: Quadrant.Value, rotation: QRotation.Value): Boolean = {
    log(s"Rotation for $quadrant by $rotation")

    quadrants(quadrant).rotate(rotation)

    _version += 1
    quadrants.tick()

    return checkVictory()
  }

  def winner = _winner

  def finished = _winner.isDefined || (freeFields == 0)

  def finishingMove = _combination

  def lines: Game#State = (0 to 5) map {
    i => if (i < Quadrant.size)
      quadrants(Quadrant.first).line(i) ++ quadrants(Quadrant.second).line(i)
    else
      quadrants(Quadrant.third).line(i % Quadrant.size) ++ quadrants(Quadrant.fourth).line(i % Quadrant.size)
  }

  def availableRotations = quadrants filter (_._2.canRotate) keys

  private def createQuadrants = Quadrant.values.toList map BoardQuadrant.named

  private def checkVictory(): Boolean = VictoryConditions.check(lines) exists {
    t => val (player, fields) = t
      _winner = Some(player)
      _combination = fields

      log(s"Game finished! $player won on $fields")
      true
  }

  private implicit class QuadrantManager(map: Map[Quadrant.Value, BoardQuadrant]) {
    def tick() = map foreach (_._2.tickCooldown())
  }
}
