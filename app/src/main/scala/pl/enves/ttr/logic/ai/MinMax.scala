package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{Move, Player, Position, Rotation}
import pl.enves.ttr.utils.ExecutorContext

import scala.collection.mutable
import scala.concurrent.Future

class MinMax(board: Board, player: Player.Value, maxTime: Int, maxDepth: Int, positionsChoosing: PositionsChoosing.Value, success: Move => Unit) extends Logging {

  private class TimedOutException extends Exception

  private class StoppedException extends Exception

  private class CacheTooBigException extends Exception

  private var stopped = false

  private val infinity = 1000000

  private val maxCachedEntries = 500000

  private var minMaxCalls = 0
  private val thisValues = mutable.HashMap[Int, Int]()
  private var thisBestMoves = mutable.HashMap[Int, Move]()
  private var previousBestMoves = mutable.HashMap[Int, Move]()

  val startTime = System.currentTimeMillis()

  def stop(): Unit = {
    this.synchronized {
      stopped = true
    }
  }

  /**
   * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning#Pseudocode
   * Instead of copying board when creating next child state, last move is reversed
   */
  def minMax(b: BoardModel, maximizing: Boolean, depth: Int, alpha: Int, beta: Int, predictedValue: Int): Int = {
    minMaxCalls += 1

    if (System.currentTimeMillis() > startTime + maxTime) {
      throw new TimedOutException
    }

    this.synchronized {
      if (stopped) {
        throw new StoppedException
      }
    }

    val signature = b.getZobristSignature
    if (thisValues.contains(signature)) {
      var value = thisValues(signature)
      if (value >= Heuristics.winnerValue || predictedValue <= -Heuristics.winnerValue) {
        value = Heuristics.winnerValue * depth
      }
      return value
    }

    // Saving return value on leaves is memory expensive, so no saving here
    if (predictedValue == Heuristics.winnerValue || predictedValue == -Heuristics.winnerValue) {
      return predictedValue * depth
    }

    def save(signature: Int, move: Move, value: Int): Unit = {
      thisValues.put(signature, value)
      thisBestMoves.put(signature, move)
      if (thisValues.size >= maxCachedEntries) {
        throw new CacheTooBigException
      }
    }

    // There is no point to search for leaves values as they are calculated in BoardModel
    if (depth == 1) {
      val best = b.findBestMove(maximizing)
      save(signature, best.move, best.value)
      return best.value
    }

    val availableMoves = if (previousBestMoves.contains(signature)) {
      b.availableMovesSorted(previousBestMoves(signature), maximizing)
    } else {
      b.availableMovesSorted(maximizing)
    }
    var al = alpha
    var be = beta
    var bestValue = 0
    var bestMove = availableMoves.head.move

    if (maximizing) {
      //Max
      bestValue = -infinity

      var i = 0
      while (i < availableMoves.size) {
        val move = availableMoves(i).move

        move match {
          case Position(x, y) => b.position(x, y, LightField.X)
          case Rotation(q, r) => b.rotate(q, r, LightField.X)
        }

        val v = minMax(b, false, depth - 1, al, be, availableMoves(i).value)
        if (v > bestValue) {
          bestValue = v
          bestMove = move
        }

        move match {
          case Position(x, y) => b.unPosition(x, y, LightField.X)
          case Rotation(q, r) => b.unRotate(q, r, LightField.X)
        }

        al = math.max(al, bestValue)
        if (al >= be) {
          save(signature, bestMove, bestValue)
          return bestValue
        }
        i += 1
      }
    } else {
      //Min
      bestValue = infinity

      var i = 0
      while (i < availableMoves.size) {
        val move = availableMoves(i).move

        move match {
          case Position(x, y) => b.position(x, y, LightField.O)
          case Rotation(q, r) => b.rotate(q, r, LightField.O)
        }

        val v = minMax(b, true, depth - 1, al, be, availableMoves(i).value)
        if (v < bestValue) {
          bestValue = v
          bestMove = move
        }

        move match {
          case Position(x, y) => b.unPosition(x, y, LightField.O)
          case Rotation(q, r) => b.unRotate(q, r, LightField.O)
        }

        be = math.min(be, bestValue)
        if (al >= be) {
          save(signature, bestMove, bestValue)
          return bestValue
        }
        i += 1
      }
    }
    save(signature, bestMove, bestValue)
    return bestValue
  }

  def printPredicted(b: BoardModel, maximizing: Boolean): Unit = {
    val sig = b.getZobristSignature
    val player = if (maximizing) LightField.X else LightField.O
    val pc = if (maximizing) "X" else "O"
    if (thisBestMoves.contains(sig)) {
      val move = thisBestMoves(sig)

      move match {
        case Position(x, y) => b.position(x, y, player)
        case Rotation(q, r) => b.rotate(q, r, player)
      }
      val boardValue = b.check()
      log(s"predicted move for $pc: $move, board value: $boardValue")
      printPredicted(b, !maximizing)

      move match {
        case Position(x, y) => b.unPosition(x, y, player)
        case Rotation(q, r) => b.unRotate(q, r, player)
      }
    }
  }

  implicit val ec = ExecutorContext.context
  private val f: Future[Move] = Future {
    val b = BoardModel(board, positionsChoosing)
    val maximizing = player == Player.X

    var depth = 1

    var bestMove = b.availableMovesSorted(maximizing).head.move
    var bestValue = 0
    var run = true
    while (depth <= maxDepth && run) {
      log(s"starting with depth: $depth")
      minMaxCalls = 0
      thisValues.clear()
      previousBestMoves = thisBestMoves
      thisBestMoves = mutable.HashMap[Int, Move]()
      try {
        val signature = b.getZobristSignature
        minMax(b, maximizing, depth, -infinity, infinity, 0)
        bestMove = thisBestMoves(signature)
        bestValue = thisValues(signature)
        val states = thisValues.size
        log(s"depth: $depth, calls: $minMaxCalls, saved states: $states, bestMove: $bestMove, bestValue: $bestValue")
        printPredicted(b, maximizing)
      } catch {
        case e: TimedOutException =>
          run = false
          error(s"timed out during depth: $depth")
        case e: CacheTooBigException =>
          run = false
          error(s"out of memory during depth: $depth")
        case e: OutOfMovesException =>
          run = false
          error(s"out of moves during depth: $depth")
      }
      depth += 1
    }
    log("finished after: " + (System.currentTimeMillis() - startTime) + "ms, bestMove: " + bestMove)

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
