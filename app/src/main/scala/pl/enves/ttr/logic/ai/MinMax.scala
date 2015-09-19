package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{Quadrant, QRotation}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class MinMax(board: Board, success: LightMove => Unit) extends Logging {

  //TODO: more heuristic optimizations
  //Like taking quadrants centers first
  //Or building symmetrical sets of figures
  //Or taking fields adjacent to already taken ones
  private def availableMoves(board: LightBoard): ArrayBuffer[LightMove] = {
    //Generally it is better to take a new field than rotate
    val moves: mutable.ArrayBuffer[LightMove] = mutable.ArrayBuffer[LightMove]()
    var quadrant = 0
    while (quadrant < 4) {
      var x = 0
      var y = 0
      while (x < Quadrant.size) {
        y = 0
        while (y < Quadrant.size) {
          if (board.quadrantField(quadrant, x, y) == 0) {
            moves.append(new LightPosition(quadrant, x, y))
          }
          y += 1
        }
        x += 1
      }
      quadrant += 1
    }
    quadrant = 0
    while (quadrant < 4) {
      if (board.canRotate(quadrant)) {
        moves.append(new LightRotation(quadrant, QRotation.r90))
        moves.append(new LightRotation(quadrant, QRotation.r270))
      }
      quadrant += 1
    }
    return moves
  }

  /**
   * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning#Pseudocode
   * Instead of copying board when creating next child state, last move is reversed
   */
  def minMax(b: LightBoard, playerMM: Int, depth: Int, alpha: Int, beta: Int): Int = {
    val opponent = LightField.opponent(playerMM)

    val stateValue = Heuristics.check(b.state())
    if (depth == 0 || stateValue == Heuristics.winnerValue || stateValue == -Heuristics.winnerValue) {
      return stateValue
    }

    val moves = availableMoves(b)

    var al = alpha
    var be = beta
    if (playerMM == LightField.X) {
      //Max
      var v = -1000000

      var i = 0
      while (i < moves.size) {

        moves(i) match {
          case LightPosition(q, x, y) => b move(q, x, y, playerMM)
          case LightRotation(q, r) => b rotate(q, r, playerMM)
        }

        v = math.max(v, minMax(b, opponent, depth - 1, al, be))
        moves(i) match {
          case LightPosition(q, x, y) => b unMove(q, x, y, playerMM)
          case LightRotation(q, r) => b unRotate(q, r, playerMM)
        }
        al = math.max(al, v)
        if (al >= be) {
          return v
        }
        i += 1
      }

      return v
    } else {
      //Min
      var v = 1000000

      var i = 0
      while (i < moves.size) {

        moves(i) match {
          case LightPosition(q, x, y) => b move(q, x, y, playerMM)
          case LightRotation(q, r) => b rotate(q, r, playerMM)
        }

        v = math.min(v, minMax(b, opponent, depth - 1, al, be))
        moves(i) match {
          case LightPosition(q, x, y) => b unMove(q, x, y, playerMM)
          case LightRotation(q, r) => b unRotate(q, r, playerMM)
        }
        be = math.min(be, v)
        if (al >= be) {
          return v
        }
        i += 1
      }

      return v
    }
  }

  private val f: Future[LightMove] = Future {
    val b = LightBoard(board)
    val moves = availableMoves(b)

    val depth = //2
      if (moves.length <= 12) 3
      else if (moves.length <= 40) 2
      else 1

    //Computer plays with Os, so first step is Min
    val al = -1000000
    var be = 1000000
    var bestMove = moves.head
    var i = 0
    while (i < moves.size) {

      moves(i) match {
        case LightPosition(q, x, y) => b move(q, x, y, LightField.O)
        case LightRotation(q, r) => b rotate(q, r, LightField.O)
      }
      val v = minMax(b, LightField.X, depth - 1, al, be)
      if (v < be) {
        be = v
        bestMove = moves(i)
      }
      moves(i) match {
        case LightPosition(q, x, y) => b unMove(q, x, y, LightField.O)
        case LightRotation(q, r) => b unRotate(q, r, LightField.O)
      }

      if (al >= be)
        error("Something went wrong")
      i += 1
    }

    bestMove
  }
  f onSuccess {
    case move => success(move)
  }

  f onFailure {
    case t => {
      error(t.getMessage)
      val nb = new NoBrainer(board, success)
    }
  }
}
