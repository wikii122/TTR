package pl.enves.ttr.logic.ai

import pl.enves.ttr.logic.{Quadrant, QRotation}
import pl.enves.ttr.logic.inner.Board

import scala.collection.mutable

class NoBrainer(board: Board, success: LightMove => Unit) {
  val moves: mutable.ArrayBuffer[LightMove] = mutable.ArrayBuffer[LightMove]()

    for (x <- 0 until Quadrant.size) {
      for (y <- 0 until Quadrant.size) {
        if (board.lines(x)(y).isEmpty) {
          moves.append(new LightPosition(x, y))
        }
      }
    }

  for(quadrant <- board.availableRotations) {
    moves.append(new LightRotation(quadrant.id, QRotation.r90))
    moves.append(new LightRotation(quadrant.id, QRotation.r270))
  }
  // Pick move at random
  val r: Int = (Math.random() * moves.size).floor.toInt

  success(moves(r))
}
