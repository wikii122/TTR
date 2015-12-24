package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class OutOfMovesException extends Exception

/**
 * Manages fields states.
 * Version without saving, safety checks, prints, and nice but time-costly things
 * With ability to reverse moves
 * TODO: Optimize more
 */
class BoardModel(positionsChoosing: PositionsChoosing.Value) extends Logging {
  private val five = for (x <- 0 until 5) yield x
  private val six = for (x <- 0 until 6) yield x
  private val two = for (x <- 0 until 2) yield x

  private val quadrants = Array(
    BoardQuadrantModel(),
    BoardQuadrantModel(),
    BoardQuadrantModel(),
    BoardQuadrantModel()
  )

  private var freeFields = 36

  val state = Array.fill[Int] (6, 6) { LightField.None }

  private val countersMap = Array.fill(6, 6) {
    ArrayBuffer[Counter]()
  }

  private val counters = prepareCounters()

  //Their purpose is reducing GC pressure by reducing fresh allocations
  private val _cached_moves_positions = Array.tabulate(6, 6) {
    (x: Int, y: Int) => new Position(x, y)
  }

  private val _cached_moves_rotations_left = Array(
    new Rotation(Quadrant.first, QRotation.r90),
    new Rotation(Quadrant.second, QRotation.r90),
    new Rotation(Quadrant.third, QRotation.r90),
    new Rotation(Quadrant.fourth, QRotation.r90)
  )

  private val _cached_moves_rotations_right = Array(
    new Rotation(Quadrant.first, QRotation.r270),
    new Rotation(Quadrant.second, QRotation.r270),
    new Rotation(Quadrant.third, QRotation.r270),
    new Rotation(Quadrant.fourth, QRotation.r270)
  )

  private val zobristTableFields = Array.fill(6, 6, 3) {
    0
  }
  private val zobristTableCooldowns = Array.fill(4, BoardQuadrantModel.rotationIndicator + 1) {
    0
  }
  private var zobristSignature: Int = 0

  initZobrist()

  //printCountersNum()
  //printCountersCoordinates()

  private def initZobrist(): Unit = {
    val generator = new Random(4) //TODO: seed
    for (x <- six) {
      for (y <- six) {
        for (i <- 0 until 3)
          zobristTableFields(x)(y)(i) = generator.nextInt()
      }
    }
    for (q <- 0 until 4) {
      for (i <- 0 until BoardQuadrantModel.rotationIndicator + 1) {
        zobristTableCooldowns(q)(i) = generator.nextInt()
      }
    }
  }

  /**
   * for tests
   */
  private def calculateZobristSignature(): Int = {
    var h: Int = 0
    var x = 0
    var y = 0
    while (x < 6) {
      y = 0
      while (y < 6) {
        h = zobristSignaturePosition(x, y, h)
        y += 1
      }
      x += 1
    }
    var q = 0
    while (q < 4) {
      h = zobristSignatureCooldown(q, h)
      q += 1
    }
    return h
  }

  private def zobristSignaturePosition(x: Int, y: Int, hash: Int): Int = {
    return state(x)(y) match {
      case LightField.None =>
        hash ^ zobristTableFields(x)(y)(0)
      case LightField.X =>
        hash ^ zobristTableFields(x)(y)(1)
      case LightField.O =>
        hash ^ zobristTableFields(x)(y)(2)
    }
  }

  private def zobristSignatureCooldown(q: Int, hash: Int): Int = {
    return hash ^ zobristTableCooldowns(q)(quadrants(q).getCooldown)
  }

  def getZobristSignature = zobristSignature

  private def prepareCounters(): Array[Counter] = {
    val buf = ArrayBuffer[Counter]()
    prepareHorizontalCounters(buf)
    prepareVerticalCounters(buf)
    prepareDiagonalCounters(buf)
    return buf.toArray
  }

  private def prepareHorizontalCounters(buf: ArrayBuffer[Counter]): Unit = {
    for (x <- two; y <- six) {
      prepareRowCounter(x, y, buf)
    }
  }

  private def prepareVerticalCounters(buf: ArrayBuffer[Counter]): Unit = {
    for (x <- six; y <- two) {
      prepareColumnCounter(x, y, buf)
    }
  }

  private def prepareDiagonalCounters(buf: ArrayBuffer[Counter]): Unit = {
    for (x <- two; y <- two) {
      prepareNormalDiagonalCounter(x, y, buf)
      prepareReverseDiagonalCounter(5 - x, y, buf)
    }
  }

  private def prepareRowCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x + i)(y).append(counter)
    })
  }

  private def prepareColumnCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x)(y + i).append(counter)
    })
  }

  private def prepareNormalDiagonalCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x + i)(y + i).append(counter)
    })
  }

  private def prepareReverseDiagonalCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x - i)(y + i).append(counter)
    })
  }

  def check(): Int = {
    var sum = 0
    var winX = 0
    var winO = 0
    var i = 0
    val countersLength = counters.length
    while (i < countersLength) {
      val v = counters(i).getValue
      if (v == Heuristics.winnerValue) {
        winX += 1
      } else if (v == -Heuristics.winnerValue) {
        winO += 1
      } else {
        sum += v
      }
      i += 1
    }

    val ret = if (winX == 0 && winO == 0) sum
    else if (winX > 0 && winO == 0) Heuristics.winnerValue
    else if (winO > 0 && winX == 0) -Heuristics.winnerValue
    else 0 //draw

    //val heuristic = Heuristics.check(state)
    //if(heuristic != ret) {
    //  error(s"heuristic: $heuristic != counters: $ret")
    //}

    //if(zobristSignature != getZobristHash) {
    //  error(s"hash error")
    //}

    return ret
  }

  def checkMove(m: Move, player: Int): Int = {
    return m match {
      case Position(x, y) => checkPosition(x, y, player)
      case Rotation(q, r) => checkRotate(q, r, player)
    }
  }

  def checkPosition(x: Int, y: Int, player: Int): Int = {
    position(x, y, player)
    val value = check()
    unPosition(x, y, player)
    return value
  }

  def checkRotate(q: Quadrant.Value, r: QRotation.Value, player: Int): Int = {
    rotate(q, r, player)
    val value = check()
    unRotate(q, r, player)
    return value
  }

  def position(x: Int, y: Int, player: Int): Unit = {
    zobristTick()
    state(x)(y) = player
    countField(x, y)
    tick()
    freeFields -= 1
    zobristTick()
  }

  def unPosition(x: Int, y: Int, player: Int): Unit = {
    zobristTick()
    freeFields += 1
    unTick()
    unCountField(x, y)
    state(x)(y) = LightField.None
    zobristTick()
  }

  def rotate(q: Quadrant.Value, rotation: QRotation.Value, player: Int): Unit = {
    val quadrant = q.id
    zobristTick()
    unCountQuadrantForRotation(quadrant)
    rotation match {
      case QRotation.r90 => rotateQuadrantLeft(quadrant)
      case QRotation.r270 => rotateQuadrantRight(quadrant)
    }
    countQuadrantForRotation(quadrant)
    quadrants(quadrant).rotate(rotation)
    tick()
    zobristTick()
  }

  def unRotate(q: Quadrant.Value, rotation: QRotation.Value, player: Int): Unit = {
    val quadrant = q.id
    zobristTick()
    unTick()
    quadrants(quadrant).unRotate(rotation)
    unCountQuadrantForRotation(quadrant)
    rotation match {
      case QRotation.r90 => rotateQuadrantRight(quadrant)
      case QRotation.r270 => rotateQuadrantLeft(quadrant)
    }
    countQuadrantForRotation(quadrant)
    zobristTick()
  }

  private def countField(x: Int, y: Int): Unit = {
    val f = state(x)(y)
    if (f != LightField.None) {
      val counters = countersMap(x)(y)
      val countersLength = counters.length
      var i = 0
      while (i < countersLength) {
        val counter = counters(i)
        counter.add(f)
        i += 1
      }
    }
    zobristSignature = zobristSignaturePosition(x, y, zobristSignature)
  }

  private def unCountField(x: Int, y: Int): Unit = {
    val f = state(x)(y)
    if (f != LightField.None) {
      val counters = countersMap(x)(y)
      val countersLength = counters.length
      var i = 0
      while (i < countersLength) {
        val counter = counters(i)
        counter.sub(f)
        i += 1
      }
    }
    zobristSignature = zobristSignaturePosition(x, y, zobristSignature)
  }

  def offsetX(quadrant: Int) = quadrant match {
    case 0 => 0
    case 1 => Quadrant.size
    case 2 => 0
    case 3 => Quadrant.size
  }

  def offsetY(quadrant: Int) = quadrant match {
    case 0 => 0
    case 1 => 0
    case 2 => Quadrant.size
    case 3 => Quadrant.size
  }

  private def countQuadrantForRotation(quadrant: Int): Unit = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)
    //only quadrant centers don't change
    countField(x + 0, y + 0)
    countField(x + 0, y + 1)
    countField(x + 0, y + 2)
    countField(x + 1, y + 0)
    countField(x + 1, y + 2)
    countField(x + 2, y + 0)
    countField(x + 2, y + 1)
    countField(x + 2, y + 2)
  }

  private def unCountQuadrantForRotation(quadrant: Int): Unit = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)
    unCountField(x + 0, y + 0)
    unCountField(x + 0, y + 1)
    unCountField(x + 0, y + 2)
    unCountField(x + 1, y + 0)
    unCountField(x + 1, y + 2)
    unCountField(x + 2, y + 0)
    unCountField(x + 2, y + 1)
    unCountField(x + 2, y + 2)
  }

  private def countBoard(): Unit = {
    var x = 0
    var y = 0
    while (x < 6) {
      y = 0
      while (y < 6) {
        countField(x, y)
        y += 1
      }
      x += 1
    }
    zobristTick()
  }

  private def rotateQuadrantLeft(quadrant: Int): Unit = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)
    //this is equivalent to rotate snake-like by 2 fields
    val t0 = state(x + 0)(y + 0)
    val t1 = state(x + 0)(y + 1)

    state(x + 0)(y + 0) = state(x + 0)(y + 2)
    state(x + 0)(y + 1) = state(x + 1)(y + 2)
    state(x + 0)(y + 2) = state(x + 2)(y + 2)
    state(x + 1)(y + 2) = state(x + 2)(y + 1)
    state(x + 2)(y + 2) = state(x + 2)(y + 0)
    state(x + 2)(y + 1) = state(x + 1)(y + 0)
    state(x + 2)(y + 0) = t0
    state(x + 1)(y + 0) = t1
  }

  private def rotateQuadrantRight(quadrant: Int): Unit = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)
    //this is equivalent to rotate snake-like by 2 fields
    val t0 = state(x + 0)(y + 0)
    val t1 = state(x + 1)(y + 0)

    state(x + 0)(y + 0) = state(x + 2)(y + 0)
    state(x + 1)(y + 0) = state(x + 2)(y + 1)
    state(x + 2)(y + 0) = state(x + 2)(y + 2)
    state(x + 2)(y + 1) = state(x + 1)(y + 2)
    state(x + 2)(y + 2) = state(x + 0)(y + 2)
    state(x + 1)(y + 2) = state(x + 0)(y + 1)
    state(x + 0)(y + 2) = t0
    state(x + 0)(y + 1) = t1
  }

  def tick(): Unit = {
    var i = 0
    while (i < 4) {
      quadrants(i).tickCooldown()
      i += 1
    }
  }

  def unTick(): Unit = {
    var i = 0
    while (i < 4) {
      quadrants(i).unTickCooldown()
      i += 1
    }
  }

  private def zobristTick(): Unit = {
    var i = 0
    while (i < 4) {
      zobristSignature = zobristSignatureCooldown(i, zobristSignature)
      i += 1
    }
  }

  private def availableMoves(maximizing: Boolean): ArrayBuffer[ValuedMove] = {
    val moves = ArrayBuffer[ValuedMove]()
    val player = if (maximizing) LightField.X else LightField.O
    if (freeFields == 36) {
      //TODO: Pick one at random
      val x = 1
      val y = 1
      val value = checkPosition(x, y, player)
      moves.append(new ValuedMove(value, _cached_moves_positions(x)(y)))
      return moves
    }

    //TODO: calculate value faster
    var x = 0
    var y = 0
    while (x < 6) {
      y = 0
      while (y < 6) {
        if (state(x)(y) == 0) {
          val value = checkPosition(x, y, player)
          moves.append(new ValuedMove(value, _cached_moves_positions(x)(y)))
        }
        y += 1
      }
      x += 1
    }

    var quadrant = 0
    while (quadrant < 4) {
      if (canRotate(quadrant) && !isRotationImmune(quadrant)) {

        val value = checkRotate(Quadrant(quadrant), QRotation.r90, player)
        moves.append(new ValuedMove(value, _cached_moves_rotations_left(quadrant)))

        val value2 = checkRotate(Quadrant(quadrant), QRotation.r270, player)
        moves.append(new ValuedMove(value2, _cached_moves_rotations_right(quadrant)))
      }
      quadrant += 1
    }

    if (moves.isEmpty) {
      throw new OutOfMovesException
    }

    return moves
  }

  def availableMovesSorted(maximizing: Boolean): ArrayBuffer[ValuedMove] = {
    val moves = availableMoves(maximizing)

    //Checking more important fields first increases possibility of alpha-beta cutoff
    var sortedValuedMoves = moves.toArray
    scala.util.Sorting.quickSort(sortedValuedMoves)
    sortedValuedMoves = if (maximizing) sortedValuedMoves else sortedValuedMoves.reverse

    val movesLength = sortedValuedMoves.length

    val moves2 = ArrayBuffer[ValuedMove]()

    //warning - omitting moves with small value may cause idiocy
    positionsChoosing match {
      case PositionsChoosing.Reasonable =>
        var i = 0
        while (i < movesLength) {
          moves2.append(sortedValuedMoves(i))
          i += 1
        }
      case PositionsChoosing.GEMedian =>
        val median = if (movesLength > 0) {
          if (movesLength % 2 == 0) {
            (sortedValuedMoves(movesLength / 2).value + sortedValuedMoves((movesLength / 2) - 1).value) / 2
          } else {
            sortedValuedMoves(movesLength / 2).value
          }
        } else {
          0
        }

        if (maximizing) {
          var i = 0
          while (i < movesLength && sortedValuedMoves(i).value >= median) {
            moves2.append(sortedValuedMoves(i))
            i += 1
          }
        } else {
          var i = 0
          while (i < movesLength && sortedValuedMoves(i).value <= median) {
            moves2.append(sortedValuedMoves(i))
            i += 1
          }
        }
      case PositionsChoosing.GEAverage =>
        var sum = 0
        var j = 0
        while (j < movesLength) {
          sum += sortedValuedMoves(j).value
          j += 1
        }
        val average = sum / movesLength
        if (maximizing) {
          var i = 0
          while (i < movesLength && sortedValuedMoves(i).value >= average) {
            moves2.append(sortedValuedMoves(i))
            i += 1
          }
        } else {
          var i = 0
          while (i < movesLength && sortedValuedMoves(i).value <= average) {
            moves2.append(sortedValuedMoves(i))
            i += 1
          }
        }
      case PositionsChoosing.Max8 =>
        var i = 0
        while (i < movesLength && i < 8) {
          moves2.append(sortedValuedMoves(i))
          i += 1
        }
      case PositionsChoosing.Max12 =>
        var i = 0
        while (i < movesLength && i < 12) {
          moves2.append(sortedValuedMoves(i))
          i += 1
        }
      case PositionsChoosing.Max16 =>
        var i = 0
        while (i < movesLength && i < 16) {
          moves2.append(sortedValuedMoves(i))
          i += 1
        }
    }

    return moves2
  }

  def availableMovesSorted(previousBest: Move, maximizing: Boolean): ArrayBuffer[ValuedMove] = {
    val moves = availableMovesSorted(maximizing)
    val player = if (maximizing) LightField.X else LightField.O
    val size = moves.size
    val moves2 = new ArrayBuffer[ValuedMove](size)
    moves2.append(new ValuedMove(checkMove(previousBest, player), previousBest))
    var i = 0
    while (i < size) {
      val move = moves(i)
      if (move.move != previousBest) {
        moves2.append(move)
      }
      i += 1
    }
    return moves2
  }

  def findBestMove(maximizing: Boolean): ValuedMove = {
    val moves = availableMovesSorted(maximizing)
    var best = 0
    var i = 1
    while (i < moves.length) {
      if (moves(i).value > moves(best).value) {
        best = i
      }
      i += 1
    }
    return moves(best)
  }

  def canRotate(quadrant: Int) = quadrants(quadrant).canRotate

  private def isRotationImmune(quadrant: Int): Boolean = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)

    //aba
    //b_b
    //aba
    state(x + 0)(y + 0) == state(x + 2)(y + 0) &&
      state(x + 2)(y + 0) == state(x + 2)(y + 2) &&
      state(x + 2)(y + 2) == state(x + 0)(y + 2) &&
      state(x + 1)(y + 0) == state(x + 2)(y + 1) &&
      state(x + 2)(y + 1) == state(x + 1)(y + 2) &&
      state(x + 1)(y + 2) == state(x + 0)(y + 1)
  }

  def getFreeFields = freeFields

  def getQuadrant(quadrant: Int) = quadrants(quadrant)

  def printCountersNum(): Unit = {
    for (y <- six) {
      val n0 = countersMap(0)(5 - y).length
      val n1 = countersMap(1)(5 - y).length
      val n2 = countersMap(2)(5 - y).length
      val n3 = countersMap(3)(5 - y).length
      val n4 = countersMap(4)(5 - y).length
      val n5 = countersMap(5)(5 - y).length
      log(s"counters num: $n0 $n1 $n2 $n3 $n4 $n5")
    }
  }

  def printState(): Unit = {
    def c(s: Int) = s match {
      case LightField.X => "X"
      case LightField.O => "O"
      case _ => "#"
    }
    for (y <- six) {
      val s0 = c(state(0)(5 - y))
      val s1 = c(state(1)(5 - y))
      val s2 = c(state(2)(5 - y))
      val s3 = c(state(3)(5 - y))
      val s4 = c(state(4)(5 - y))
      val s5 = c(state(5)(5 - y))
      log(s"state: $s0 $s1 $s2 $s3 $s4 $s5")
    }
  }
}

object BoardModel {
  def apply(positionsChoosing: PositionsChoosing.Value) = new BoardModel(positionsChoosing)

  def apply(old: Board, positionsChoosing: PositionsChoosing.Value): BoardModel = {
    val board = new BoardModel(positionsChoosing)
    board.freeFields = old.getFreeFields

    for (q <- Quadrant.values) {
      val oldq = old.getQuadrant(q)
      val newq = board.quadrants(q.id)
      newq.setCooldown(oldq.getCooldown)
    }
    val state = old.lines
    for (i <- 0 until 2 * Quadrant.size;
         j <- 0 until 2 * Quadrant.size
    ) board.state(i)(j) = LightField.fromOption(state(j)(i))

    board.countBoard()

    return board
  }
}
