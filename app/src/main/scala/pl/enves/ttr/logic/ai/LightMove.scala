package pl.enves.ttr.logic.ai

import pl.enves.ttr.logic.{Game, QRotation, Quadrant}

class LightMove

case class LightPosition(x: Int, y: Int) extends LightMove

case class LightRotation(quadrant: Int, r: QRotation.Value) extends LightMove

object LightMove {
  def toNormalMove(lm: LightMove, game: Game): Game#Move = lm match {
    case LightPosition(x, y) => new game.Position(x, y)
    case LightRotation(q, r) => new game.Rotation(Quadrant(q), r)
  }
}
