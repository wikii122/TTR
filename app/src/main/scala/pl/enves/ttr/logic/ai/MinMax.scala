package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{Move, Player, Position, Rotation}
import pl.enves.ttr.utils.ExecutorContext

import scala.collection.mutable
import scala.concurrent.Future

class MinMax(board: Board, player: Player.Value, maxTime: Int, maxDepth: Int,
             randomize: Boolean,
             success: Move => Unit) extends Logging {

  private class TimedOutException extends Exception

  private class StoppedException extends Exception

  private class CacheTooBigException extends Exception

  private var stopped = false

  private val maxCachedEntries = 40000

  private case class ValueDepth(value: Int, depth: Int)

  private var minMaxCalls = 0
  private val thisValues = mutable.HashMap[Int, ValueDepth]()
  private var thisBestMoves = mutable.HashMap[Int, Move]()
  private var previousBestMoves = mutable.HashMap[Int, Move]()

  private val startTime = System.currentTimeMillis()

  private val boardModel = BoardModel(board)

  def stop(): Unit = {
    this.synchronized {
      stopped = true
    }
  }

  private def save(signature: Int, move: Move, value: Int, depth: Int): Unit = {
    thisValues.put(signature, new ValueDepth(value, depth))
    thisBestMoves.put(signature, move)
    if (thisValues.size >= maxCachedEntries) {
      throw new CacheTooBigException
    }
  }

  private def availableMoves(signature: Int, maximizing: Boolean, depth: Int): Array[ValuedMove] = {
    return if (previousBestMoves.contains(signature)) {
      boardModel.availableMovesSorted(previousBestMoves(signature), maximizing)
    } else {
      boardModel.availableMovesSorted(maximizing)
    }
  }

  /**
   * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning#Pseudocode
   * Instead of copying board when creating next child state, last move is reversed
   */
  private def minMax(depth: Int, alpha: Int, beta: Int, maximizing: Boolean): ValueDepth = {
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

    if (thisValues.contains(signature)) {
      // we visited this state before
      val c = thisValues(signature)
      // use calculated value only if provides no-worse insight
      if (c.depth >= depth) {
        return new ValueDepth(c.value, depth)
      }
    }

    // There is no point to search for leaves values as they are calculated in BoardModel
    if (depth == 1) {
      val best = boardModel.findBestMove(alpha, beta, maximizing, randomize)
      save(signature, best.move, best.value, depth)
      return new ValueDepth(best.value, depth)
    }

    val moves = availableMoves(signature, maximizing, depth)

    var al = alpha
    var be = beta
    var bestValue = if (maximizing) -BoardModel.infinity else BoardModel.infinity
    var bestMove = moves.head.move
    var bestDepth = depth

    val symbol = if (maximizing) LightField.X else LightField.O

    var i = 0
    while (i < moves.length) {
      val move = moves(i).move
      val predictedValue = moves(i).value
      val freeFieldsAfter = move match {
        case Position(x, y) => boardModel.getFreeFields - 1
        case Rotation(q, r) => boardModel.getFreeFields
      }

      val calculated = if (Math.abs(predictedValue) == BoardModel.winnerValue || freeFieldsAfter == 0) {
        new ValueDepth(predictedValue, depth)
      } else {

        move match {
          case Position(x, y) => boardModel.position(x, y, symbol)
          case Rotation(q, r) => boardModel.rotate(q, r, symbol)
        }

        val vd = minMax(depth - 1, al, be, !maximizing)

        move match {
          case Position(x, y) => boardModel.unPosition(x, y, symbol)
          case Rotation(q, r) => boardModel.unRotate(q, r, symbol)
        }

        vd
      }

      val calculatedValue = calculated.value
      val calculatedDepth = calculated.depth


      if (maximizing) {
        if (calculatedValue > bestValue) {
          bestValue = calculatedValue
          bestDepth = calculatedDepth
          bestMove = move

          al = math.max(al, bestValue)
        }
      } else {
        if (calculatedValue < bestValue) {
          bestValue = calculatedValue
          bestDepth = calculatedDepth
          bestMove = move

          be = math.min(be, bestValue)
        }
      }

      if (al >= be) {
        save(signature, bestMove, bestValue, depth)
        return new ValueDepth(bestValue, bestDepth)
      }
      i += 1
    }

    save(signature, bestMove, bestValue, depth)
    return new ValueDepth(bestValue, bestDepth)
  }

  private def minMaxStart(depth: Int, alpha: Int, beta: Int, maximizing: Boolean): ValuedMove = {

    val signature = boardModel.getZobristSignature

    // There is no point to search for leaves values as they are calculated in BoardModel
    if (depth == 1) {
      val best = boardModel.findBestMove(alpha, beta, maximizing, randomize)
      save(signature, best.move, best.value, depth)
      return best
    }

    val moves = availableMoves(signature, maximizing, depth)

    var al = alpha
    var be = beta
    var bestValue = if (maximizing) -BoardModel.infinity else BoardModel.infinity
    var bestMove: Move = new Move
    var bestDepth = depth

    val symbol = if (maximizing) LightField.X else LightField.O

    var i = 0
    while (i < moves.length) {
      val move = moves(i).move
      val predictedValue = moves(i).value
      val freeFieldsAfter = move match {
        case Position(x, y) => boardModel.getFreeFields - 1
        case Rotation(q, r) => boardModel.getFreeFields
      }

      val calculated = if (Math.abs(predictedValue) == BoardModel.winnerValue || freeFieldsAfter == 0) {
        new ValueDepth(predictedValue, depth)
      } else {

        move match {
          case Position(x, y) => boardModel.position(x, y, symbol)
          case Rotation(q, r) => boardModel.rotate(q, r, symbol)
        }

        val vd = minMax(depth - 1, al, be, !maximizing)

        move match {
          case Position(x, y) => boardModel.unPosition(x, y, symbol)
          case Rotation(q, r) => boardModel.unRotate(q, r, symbol)
        }

        vd
      }

      val calculatedValue = calculated.value
      val calculatedDepth = calculated.depth

      if (maximizing) {
        if (calculatedValue > bestValue) {
          bestValue = calculatedValue
          bestDepth = calculatedDepth
          bestMove = move

          al = math.max(al, bestValue)
        }
      } else {
        if (calculatedValue < bestValue) {
          bestValue = calculatedValue
          bestDepth = calculatedDepth
          bestMove = move

          be = math.min(be, bestValue)
        }
      }

      i += 1
    }

    save(signature, bestMove, bestValue, depth)
    return new ValuedMove(bestValue, bestMove)
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

      try {
        val signature = boardModel.getZobristSignature
        val vm = minMaxStart(depth, -BoardModel.infinity, BoardModel.infinity, maximizing)
        bestValue = vm.value

        if (Math.abs(vm.value) != BoardModel.winnerValue) {
          bestMove = vm.move
        } else {
          // there is no point to analyze more
          run = false
          if ((maximizing && bestValue == BoardModel.winnerValue) ||
            (!maximizing && bestValue == -BoardModel.winnerValue) ||
            (depth == 1)) {
            //either we know how to win or we have to take fatal move
            bestMove = vm.move
          }
        }

        val states = thisValues.size
        val time = System.currentTimeMillis() - startTime
        log(s"depth: $depth, calls: $minMaxCalls, saved states: $states, bestMove: $bestMove, bestValue: $bestValue, signature: $signature, time: $time")
        boardModel.printState()
        printPredicted(maximizing)
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
      val nb = new NoBrainer(board, success)
  }
}
