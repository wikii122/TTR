package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{Move, Player, Position, Rotation}
import pl.enves.ttr.utils.ExecutorContext

import scala.collection.mutable
import scala.concurrent.Future

class MinMax(board: Board, player: Player.Value, maxTime: Int, maxDepth: Int,
             positionsChoosing: PositionsChoosing.Value,
             bestMoveHeuristics: BestMoveHeuristics.Value,
             displayStatus: Option[String => Unit],
             success: Move => Unit) extends Logging {

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

  private val killers = Array.fill[Option[Move]](maxDepth+1) { None }

  private val startTime = System.currentTimeMillis()

  private val boardModel = BoardModel(board, positionsChoosing)

  def stop(): Unit = {
    this.synchronized {
      stopped = true
    }
  }

  /**
   * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning#Pseudocode
   * Instead of copying board when creating next child state, last move is reversed
   */
  def minMax(depth: Int, alpha: Int, beta: Int, maximizing: Boolean): Int = {
    minMaxCalls += 1

    if (System.currentTimeMillis() > startTime + maxTime) {
      throw new TimedOutException
    }

    this.synchronized {
      if (stopped) {
        throw new StoppedException
      }
    }

    val signature = boardModel.getZobristSignature

    def adjustWinnerValue(value: Int): Int = {
      if (value > 0) {
        Heuristics.winnerValue * depth
      } else {
        -Heuristics.winnerValue * depth
      }
    }

    def adjustValue(value: Int): Int = {
      if (Math.abs(value) >= Heuristics.winnerValue) {
        adjustWinnerValue(value)
      } else {
        value
      }
    }

    if (thisValues.contains(signature)) {
      // we visited this state before
      val value = thisValues(signature)
      return adjustValue(value)
    }

    def save(signature: Int, move: Move, value: Int): Unit = {
      thisValues.put(signature, value)
      thisBestMoves.put(signature, move)
      if (thisValues.size >= maxCachedEntries) {
        throw new CacheTooBigException
      }
      if (bestMoveHeuristics == BestMoveHeuristics.Killer) {
        killers(depth) = Some(move)
      }
    }

    // There is no point to search for leaves values as they are calculated in BoardModel
    if (depth == 1) {
      // no need to adjust value if the state is winning, as it would be multiplication by 1
      val best = boardModel.findBestMove(maximizing)
      save(signature, best.move, best.value)
      return best.value
    }

    val availableMoves = bestMoveHeuristics match {
      case BestMoveHeuristics.PreviousIteration =>
        if (previousBestMoves.contains(signature)) {
          boardModel.availableMovesSorted(previousBestMoves(signature), maximizing)
        } else {
          boardModel.availableMovesSorted(maximizing)
        }
      case BestMoveHeuristics.Killer =>
        if (killers(depth).isDefined && boardModel.isLegal(killers(depth).get)) {
          boardModel.availableMovesSorted(killers(depth).get, maximizing)
        } else {
          boardModel.availableMovesSorted(maximizing)
        }
      case BestMoveHeuristics.None =>
        boardModel.availableMovesSorted(maximizing)
    }

    var al = alpha
    var be = beta
    var bestValue = if (maximizing) -infinity else infinity
    var bestMove = availableMoves.head.move

    val symbol = if (maximizing) LightField.X else LightField.O

    var i = 0
    while (i < availableMoves.size) {
      val move = availableMoves(i).move
      val predictedValue = availableMoves(i).value
      val calculatedValue = if (Math.abs(predictedValue) == Heuristics.winnerValue) {
        adjustWinnerValue(predictedValue)
      } else {

        move match {
          case Position(x, y) => boardModel.position(x, y, symbol)
          case Rotation(q, r) => boardModel.rotate(q, r, symbol)
        }

        val v = minMax(depth - 1, al, be, !maximizing)

        move match {
          case Position(x, y) => boardModel.unPosition(x, y, symbol)
          case Rotation(q, r) => boardModel.unRotate(q, r, symbol)
        }

        v
      }

      if (maximizing) {
        if (calculatedValue > bestValue) {
          bestValue = calculatedValue
          bestMove = move
        }
        al = math.max(al, bestValue)
      } else {
        if (calculatedValue < bestValue) {
          bestValue = calculatedValue
          bestMove = move
        }
        be = math.min(be, bestValue)
      }

      if (al >= be) {
        save(signature, bestMove, bestValue)
        return bestValue
      }
      i += 1
    }

    save(signature, bestMove, bestValue)
    return bestValue
  }

  def printPredicted(maximizing: Boolean): Unit = {
    val sig = boardModel.getZobristSignature
    val player = if (maximizing) LightField.X else LightField.O
    val pc = if (maximizing) "X" else "O"
    if (thisBestMoves.contains(sig)) {
      val move = thisBestMoves(sig)

      move match {
        case Position(x, y) => boardModel.position(x, y, player)
        case Rotation(q, r) => boardModel.rotate(q, r, player)
      }
      val boardValue = boardModel.check()
      log(s"predicted move for $pc: $move, board value: $boardValue")
      printPredicted(!maximizing)

      move match {
        case Position(x, y) => boardModel.unPosition(x, y, player)
        case Rotation(q, r) => boardModel.unRotate(q, r, player)
      }
    }
  }

  implicit val ec = ExecutorContext.context
  private val f: Future[Move] = Future {
    val maximizing = player == Player.X

    var depth = 1

    var bestMove = boardModel.availableMovesSorted(maximizing).head.move
    var bestValue = 0
    var run = true
    while (depth <= maxDepth && run) {
      log(s"starting with depth: $depth")
      minMaxCalls = 0

      //prepare hash tables
      thisValues.clear()
      previousBestMoves = thisBestMoves
      thisBestMoves = mutable.HashMap[Int, Move]()

      //clear killers
      var i = 0
      while (i <= maxDepth) {
        killers(i) = None
        i += 1
      }

      try {
        val signature = boardModel.getZobristSignature
        minMax(depth, -infinity, infinity, maximizing)
        bestMove = thisBestMoves(signature)
        bestValue = thisValues(signature)
        val states = thisValues.size
        val time = System.currentTimeMillis() - startTime
        log(s"depth: $depth, calls: $minMaxCalls, saved states: $states, bestMove: $bestMove, bestValue: $bestValue, signature: $signature, time: $time")
        if (displayStatus.isDefined) {
          displayStatus.get(s"depth: $depth, bestMove: $bestMove, bestValue: $bestValue, time: $time")
        }
        boardModel.printState()
        printPredicted(maximizing)
      } catch {
        case e: TimedOutException =>
          run = false
          error(s"timed out during depth: $depth")
          if (displayStatus.isDefined) {
            displayStatus.get(s"timed out during depth: $depth, bestMove: $bestMove, bestValue: $bestValue")
          }
        case e: CacheTooBigException =>
          run = false
          error(s"out of memory during depth: $depth")
          if (displayStatus.isDefined) {
            displayStatus.get(s"out of memory during depth: $depth, bestMove: $bestMove, bestValue: $bestValue")
          }
        case e: OutOfMovesException =>
          run = false
          error(s"out of moves during depth: $depth")
          if (displayStatus.isDefined) {
            displayStatus.get(s"out of moves during depth: $depth, bestMove: $bestMove, bestValue: $bestValue")
          }
      }
      depth += 1
    }
    log(s"finished, bestMove: $bestMove")

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
      if (displayStatus.isDefined) {
        displayStatus.get(s"switching to random")
      }
      val nb = new NoBrainer(board, success)
  }
}
