package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{Player, QRotation, Quadrant}
import pl.enves.ttr.utils.ExecutorContext

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

class MinMax(board: Board, player: Player.Value, maxTime: Int, maxDepth: Int, success: LightMove => Unit) extends Logging {

  private class TimedOutException extends RuntimeException

  private class StoppedException extends RuntimeException

  private var stopped = false

  def stop(): Unit = {
    this.synchronized {
      stopped = true
    }
  }

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
  def minMax(b: LightBoard, playerMM: Int, depth: Int, alpha: Int, beta: Int, startTime: Long): Int = {
    if (System.currentTimeMillis() > startTime + maxTime) {
      throw new TimedOutException
    }

    this.synchronized {
      if (stopped) {
        throw new StoppedException
      }
    }

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

        v = math.max(v, minMax(b, opponent, depth - 1, al, be, startTime))
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

        v = math.min(v, minMax(b, opponent, depth - 1, al, be, startTime))
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

  private def startO(b: LightBoard, moves: ArrayBuffer[LightMove], depth: Int, alpha: Int, beta: Int, startTime: Long): LightMove = {
    val al = alpha
    var be = beta
    var bestMove = moves.head
    var i = 0
    while (i < moves.size) {
      moves(i) match {
        case LightPosition(q, x, y) => b move(q, x, y, LightField.O)
        case LightRotation(q, r) => b rotate(q, r, LightField.O)
      }
      val v = minMax(b, LightField.X, depth - 1, al, be, startTime)
      if (v < be) {
        be = v
        bestMove = moves(i)
      }
      moves(i) match {
        case LightPosition(q, x, y) => b unMove(q, x, y, LightField.O)
        case LightRotation(q, r) => b unRotate(q, r, LightField.O)
      }

      i += 1
    }

    return bestMove
  }

  private def startX(b: LightBoard, moves: ArrayBuffer[LightMove], depth: Int, alpha: Int, beta: Int, startTime: Long): LightMove = {
    var al = alpha
    val be = beta
    var bestMove = moves.head
    var i = 0
    while (i < moves.size) {
      moves(i) match {
        case LightPosition(q, x, y) => b move(q, x, y, LightField.X)
        case LightRotation(q, r) => b rotate(q, r, LightField.X)
      }
      val v = minMax(b, LightField.O, depth - 1, al, be, startTime)
      if (v > al) {
        al = v
        bestMove = moves(i)
      }
      moves(i) match {
        case LightPosition(q, x, y) => b unMove(q, x, y, LightField.X)
        case LightRotation(q, r) => b unRotate(q, r, LightField.X)
      }
      i += 1
    }

    return bestMove
  }

  implicit val ec = ExecutorContext.context
  private val f: Future[LightMove] = Future {
    val startTime = System.currentTimeMillis()
    val b = LightBoard(board)
    val moves = availableMoves(b)
    val al = -1000000
    val be = 1000000

    var depth = 1

    var bestMove = moves.head
    var run = true
    while (depth <= maxDepth && run) {
      try {
        bestMove = if (player == Player.X) {
          startX(b, moves, depth, al, be, startTime)
        } else {
          startO(b, moves, depth, al, be, startTime)
        }
      } catch {
        case e: TimedOutException =>
          run = false
          log("timed out after: " + (System.currentTimeMillis() - startTime) + "ms, depth: " + depth)
      }
      depth += 1
    }
    bestMove
  }
  f onSuccess {
    case move =>
      success(move)
  }
  f onFailure {
    case e: StoppedException =>
      log("stopped")
    case _ =>
      log("switching to random")
      val nb = new NoBrainer(board, success)
  }
}
