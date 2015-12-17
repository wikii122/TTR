package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.Player
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.ExecutorContext

import scala.concurrent.Future

class MinMax(board: Board, player: Player.Value, maxTime: Int, maxDepth: Int, positionsChoosing: PositionsChoosing.Value, success: LightMove => Unit) extends Logging {

  private class TimedOutException extends RuntimeException

  private class StoppedException extends RuntimeException

  private var stopped = false

  private val infinity = 1000000

  def stop(): Unit = {
    this.synchronized {
      stopped = true
    }
  }

  /**
   * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning#Pseudocode
   * Instead of copying board when creating next child state, last move is reversed
   */
  def minMax(b: BoardModel, playerMM: Int, depth: Int, alpha: Int, beta: Int, startTime: Long): Int = {
    if (System.currentTimeMillis() > startTime + maxTime) {
      throw new TimedOutException
    }

    this.synchronized {
      if (stopped) {
        throw new StoppedException
      }
    }

    val opponent = LightField.opponent(playerMM)

    val stateValue = b.check()

    if(stateValue == Heuristics.winnerValue || stateValue == -Heuristics.winnerValue) {
      return stateValue * (depth + 1)
    }

    if (depth == 0) {
      return stateValue
    }

    val availableMoves = b.availableMoves()

    var al = alpha
    var be = beta
    var bestValue = 0

    if (playerMM == LightField.X) {
      //Max
      bestValue = -infinity

      var i = 0
      while (i < availableMoves.size) {
        val move = availableMoves(i)
        move match {
          case LightPosition(x, y) => b move(x, y, playerMM)
          case LightRotation(q, r) => b rotate(q, r, playerMM)
        }

        bestValue = math.max(bestValue, minMax(b, opponent, depth - 1, al, be, startTime))

        move match {
          case LightPosition(x, y) => b unMove(x, y, playerMM)
          case LightRotation(q, r) => b unRotate(q, r, playerMM)
        }

        al = math.max(al, bestValue)
        if (al >= be) {
          return bestValue
        }
        i += 1
      }
    } else {
      //Min
      bestValue = infinity

      var i = 0
      while (i < availableMoves.size) {
        val move = availableMoves(i)

        move match {
          case LightPosition(x, y) => b move(x, y, playerMM)
          case LightRotation(q, r) => b rotate(q, r, playerMM)
        }

        bestValue = math.min(bestValue, minMax(b, opponent, depth - 1, al, be, startTime))

        move match {
          case LightPosition(x, y) => b unMove(x, y, playerMM)
          case LightRotation(q, r) => b unRotate(q, r, playerMM)
        }

        be = math.min(be, bestValue)
        if (al >= be) {
          return bestValue
        }
        i += 1
      }
    }
    return bestValue
  }

  def minMaxStart(b: BoardModel, playerMM: Int, depth: Int, startTime: Long): (Int, LightMove) = {

    val opponent = LightField.opponent(playerMM)

    val availableMoves = b.availableMoves()

    val al = -infinity
    val be = infinity
    var bestValue = 0
    var bestMove = availableMoves.head

    //TODO: remove in production
    val researchPositionValues = Array.fill(6, 6) { "  #  " }

    if (playerMM == LightField.X) {
      //Max
      bestValue = -infinity

      var i = 0
      while (i < availableMoves.size) {
        val move = availableMoves(i)
        move match {
          case LightPosition(x, y) => b move(x, y, playerMM)
          case LightRotation(q, r) => b rotate(q, r, playerMM)
        }

        val v = minMax(b, opponent, depth - 1, al, be, startTime)
        if (v > bestValue) {
          bestValue = v
          bestMove = move
        }

        move match {
          case LightPosition(x, y) => b unMove(x, y, playerMM)
          case LightRotation(q, r) => b unRotate(q, r, playerMM)
        }

        move match {
          case LightPosition(x, y) => researchPositionValues(x)(y) = "%05d".format(v)
          case LightRotation(q, r) =>
        }

        //There won't be any cutoffs, as beta is still max
        i += 1
      }
    } else {
      //Min
      bestValue = infinity

      var i = 0
      while (i < availableMoves.size) {
        val move = availableMoves(i)

        move match {
          case LightPosition(x, y) => b move(x, y, playerMM)
          case LightRotation(q, r) => b rotate(q, r, playerMM)
        }

        val v = minMax(b, opponent, depth - 1, al, be, startTime)
        if (v < bestValue) {
          bestValue = v
          bestMove = move
        }

        move match {
          case LightPosition(x, y) => b unMove(x, y, playerMM)
          case LightRotation(q, r) => b unRotate(q, r, playerMM)
        }

        move match {
          case LightPosition(x, y) => researchPositionValues(x)(y) = "%05d".format(v)
          case LightRotation(q, r) =>
        }

        //There won't be any cutoffs, as alpha is still min
        i += 1
      }
    }

    for (y <- 0 until 6) {
      val s0 = researchPositionValues(0)(5 - y)
      val s1 = researchPositionValues(1)(5 - y)
      val s2 = researchPositionValues(2)(5 - y)
      val s3 = researchPositionValues(3)(5 - y)
      val s4 = researchPositionValues(4)(5 - y)
      val s5 = researchPositionValues(5)(5 - y)
      log(s"values: $s0 $s1 $s2 $s3 $s4 $s5")
    }

    return (bestValue, bestMove)
  }

  implicit val ec = ExecutorContext.context
  private val f: Future[LightMove] = Future {
    val startTime = System.currentTimeMillis()
    val b = BoardModel(board, positionsChoosing)
    val p = if (player == Player.X) LightField.X else LightField.O

    var depth = 1

    var bestMove = b.availableMoves().head
    var bestValue = 0
    var run = true
    while (depth <= maxDepth && run) {
      log(s"starting with depth: $depth")
      try {
        val (v, m) = minMaxStart(b, p, depth, startTime)
        bestMove = m
        bestValue = v
        log(s"depth: $depth, bestMove: $bestMove, bestValue: $bestValue")
      } catch {
        case e: TimedOutException =>
          run = false
          log(s"timed out during depth: $depth")
      }
      depth += 1
    }
    log("finished after: " + (System.currentTimeMillis() - startTime) + "ms, bestMove: " + bestMove)

    b.printImportance()

    bestMove
  }
  f onSuccess {
    case move =>
      success(move)
  }
  f onFailure {
    case e: StoppedException =>
      log("stopped")
    case e: Exception =>
      e.printStackTrace()
      log("switching to random")
      val nb = new NoBrainer(board, success)
  }
}
