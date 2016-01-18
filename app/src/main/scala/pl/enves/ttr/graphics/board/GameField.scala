package pl.enves.ttr.graphics.board

import pl.enves.ttr.graphics.Resources
import pl.enves.ttr.logic._

class GameField(game: Game, quadrant: Quadrant.Value, boardX: Int, boardY: Int, resources: Resources) extends Field(game, quadrant, resources) {
  override protected def onClick(): Unit = {
    try {
      val move = new Position(boardX, boardY)
      game.make(move)
      discardIllegal()
    } catch {
      case e: FieldTaken =>
        setIllegal()
      case e: BoardLocked =>
        setIllegal()
    }
  }
}
