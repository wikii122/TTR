package pl.enves.ttr.logic.ai

import pl.enves.ttr.logic.{Game, QRotation, Quadrant}

class LightMove

case class LightPosition(quadrant: Int, x: Int, y: Int) extends LightMove

case class LightRotation(quadrant: Int, r: QRotation.Value) extends LightMove

object LightMove {
  def toNormalMove(lm: LightMove, game: Game): Game#Move = lm match {
    case LightPosition(q, x, y) => {
      val quadrant = Quadrant(q)
      val offset = Quadrant.offset(quadrant)
      new game.Position(x + offset._1, y + offset._2)
    }
    case LightRotation(q, r) => new game.Rotation(Quadrant(q), r)
  }
}
