package pl.enves.ttr.logic.ai

import scala.collection.mutable

/**
 * Copied from VictoryConditions and adapted
 * X is maximizing
 * O is minimizing
 * TODO: speed up
 * TODO: Consider sequences of fields after possible rotation
 */
object Heuristics {
  private val five = for (x <- 0 until 5) yield x
  private val six = for (x <- 0 until 6) yield x
  private val two = for (x <- 0 until 2) yield x
  private val sequences: mutable.ArrayBuffer[Array[(Int, Int)]] = mutable.ArrayBuffer[Array[(Int, Int)]]()

  val winnerValue = 10000
  prepareVerticalSequences()
  prepareHorizontalSeqences()
  prepareDiagonalSequences()

  def check(board: Array[Array[Int]]): Int = {
    var sum = 0
    var winX = 0
    var winO = 0
    var i = 0
    while (i < sequences.size) {
      val v = checkSeq(sequences(i), board)
      if (v == winnerValue) {
        winX += 1
      } else if (v == -winnerValue) {
        winO += 1
      } else {
        sum += v
      }
      i += 1
    }

    return if (winX == 0 && winO == 0) sum
    else if (winX > 0 && winO == 0) winnerValue
    else if (winO > 0 && winX == 0) -winnerValue
    else 0 //draw
  }

  private def prepareHorizontalSeqences(): Unit = {
    for (x <- two; y <- six)
      prepereRowSequence(x, y)
  }

  private def prepareVerticalSequences(): Unit = {
    for (x <- six; y <- two)
      prepareColumnSequence(x, y)
  }

  private def prepareDiagonalSequences(): Unit = {
    for (x <- two; y <- two) {
      prepareNormalDiagonalSequence(x, y)
      prepareReverseDiagonalSequence(5 - x, y)
    }
  }

  private def prepereRowSequence(x: Int, y: Int): Unit = {
    sequences.append((five map (i => (x + i, y))).toArray)
  }

  private def prepareColumnSequence(x: Int, y: Int): Unit = {
    sequences.append((five map (i => (x, y + i))).toArray)
  }

  private def prepareNormalDiagonalSequence(x: Int, y: Int): Unit = {
    sequences.append((five map (i => (x + i, y + i))).toArray)
  }

  private def prepareReverseDiagonalSequence(x: Int, y: Int): Unit = {
    sequences.append((five map (i => (x - i, y + i))).toArray)
  }

  private def checkSeq(seq: Array[(Int, Int)], board: Array[Array[Int]]): Int = {
    var x = 0
    var o = 0
    var i = 0
    while (i < 5) {
      val coords = seq(i)
      val field = board(coords._1)(coords._2)
      if (field == LightField.X) {
        x += 1
      } else if (field == LightField.O) {
        o += 1
      }
      i += 1
    }

    return if (x == 5) winnerValue
    else if (o == 5) -winnerValue
    else if (o == 0 && x > 0) x * x * x
    else if (x == 0 && o > 0) -(o * o * o)
    else 0
  }
}
