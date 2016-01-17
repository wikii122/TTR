package pl.enves.ttr.logic.ai

import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board

import scala.collection.mutable

class NoBrainer(board: Board, success: Move => Unit) {
  private[this] val moves: mutable.ArrayBuffer[Move] = mutable.ArrayBuffer[Move]()

  for (x <- 0 until Quadrant.size) {
    for (y <- 0 until Quadrant.size) {
      if (board.lines(x)(y).isEmpty) {
        moves.append(new Position(y, x))
      }
    }
  }

  for (quadrant <- board.availableRotations) {
    moves.append(new Rotation(quadrant, QRotation.r90))
    moves.append(new Rotation(quadrant, QRotation.r270))
  }
  // Pick move at random
  val r: Int = (Math.random() * moves.size).floor.toInt

  success(moves(r))
}
