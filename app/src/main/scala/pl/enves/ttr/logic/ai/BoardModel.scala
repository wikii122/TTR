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

  private val quadrants = Array.fill(4) {
    BoardQuadrantModel()
  }

  private var freeFields = 36

  val state = Array.fill[Int] (6, 6) { LightField.None }

  private val baseCountersMap = Array.fill(6, 6) {
    ArrayBuffer[Counter]()
  }

  private val baseCounters = prepareBaseCounters(baseCountersMap)

  private val rotatedCountersMap = Array.fill(6, 6) {
    ArrayBuffer[Counter]()
  }

  private val rotatedLeftCounters = Array.tabulate(4) { quadrant =>
    prepareRotateCounters(Quadrant(quadrant), QRotation.r90, baseCounters, rotatedCountersMap)
  }

  private val rotatedRightCounters = Array.tabulate(4) { quadrant =>
    prepareRotateCounters(Quadrant(quadrant), QRotation.r270, baseCounters, rotatedCountersMap)
  }

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

  private val generator = new Random()

  printCountersNum(baseCountersMap)
  printCountersNum(rotatedCountersMap)
  //printCountersCoordinates()
  for (quadrant <- Quadrant.values) {
    val ll = rotatedLeftCounters(quadrant.id).length
    val rl = rotatedRightCounters(quadrant.id).length
    log(s"rotatedLeftCounters($quadrant).length = $ll")
    log(s"rotatedRightCounters($quadrant).length = $rl")
  }

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
        hash ^ ZobristTable.fields(x)(y)(0)
      case LightField.X =>
        hash ^ ZobristTable.fields(x)(y)(1)
      case LightField.O =>
        hash ^ ZobristTable.fields(x)(y)(2)
    }
  }

  private def zobristSignatureCooldown(q: Int, hash: Int): Int = {
    return hash ^ ZobristTable.cooldowns(q)(quadrants(q).getCooldown)
  }

  def getZobristSignature = calculateZobristSignature()

  private def prepareBaseCounters(map: Array[Array[ArrayBuffer[Counter]]]): Array[Counter] = {
    val buf = ArrayBuffer[Counter]()
    prepareCounterSequences((seq: Seq[(Int, Int)], num: Int) => {
      val counter = new Counter()
      buf.append(counter)
      for (i <- seq) {
        map(i._1)(i._2).append(counter)
      }
    })
    return buf.toArray
  }

  private def isIn(x: Int, y: Int, quadrant: Quadrant.Value): Boolean = quadrant match {
    case Quadrant.first => x < 3 && y < 3
    case Quadrant.second => x >= 3 && y < 3
    case Quadrant.third => x < 3 && y >= 3
    case Quadrant.fourth => x >= 3 && y >= 3
  }

  private def isThrough(seq: Seq[(Int, Int)], quadrant: Quadrant.Value): Boolean = {
    var through = false
    for (i <- seq) {
      through |= isIn(i._1, i._2, quadrant)
    }
    return through
  }

  private def substituteIfInQuadrant(quadrant: Quadrant.Value, xOld: Int, yOld: Int, rot: QRotation.Value): (Int, Int) = {
    val (xBase, yBase) = rot match {
      case QRotation.r90 => (2 - yOld % 3, xOld % 3)
      case QRotation.r270 => (yOld % 3, 2 - xOld % 3)
    }
    return quadrant match {
      case Quadrant.first => if (xOld < 3 && yOld < 3) (xBase, yBase) else (xOld, yOld)
      case Quadrant.second => if (xOld >= 3 && yOld < 3) (xBase + 3, yBase) else (xOld, yOld)
      case Quadrant.third => if (xOld < 3 && yOld >= 3) (xBase, yBase + 3) else (xOld, yOld)
      case Quadrant.fourth => if (xOld >= 3 && yOld >= 3) (xBase + 3, yBase + 3) else (xOld, yOld)
    }
  }

  private def prepareRotateCounters(quadrant: Quadrant.Value,
                                    rot: QRotation.Value,
                                    base: Array[Counter],
                                    map: Array[Array[ArrayBuffer[Counter]]]): Array[Counter] = {
    val buf = ArrayBuffer[Counter]()
    prepareCounterSequences((seq: Seq[(Int, Int)], num: Int) => {
      //check if sequence passes through quadrant
      if (isThrough(seq, quadrant)) {
        //sequence is affected, need new counter
        val counter = new Counter()
        buf.append(counter)
        for (i <- seq) {
          val (nx, ny) = substituteIfInQuadrant(quadrant, i._1, i._2, rot)
          rotatedCountersMap(nx)(ny).append(counter)
        }
      } else {
        buf.append(base(num))
      }
    })
    return buf.toArray
  }

  private def prepareCounterSequences(f: (Seq[(Int, Int)], Int) => Unit): Unit = {
    var num = 0

    def prepareHorizontal(): Unit = {
      for (x <- two; y <- six) {
        prepareRow(x, y)
      }
    }

    def prepareVertical(): Unit = {
      for (x <- six; y <- two) {
        prepareColumn(x, y)
      }
    }

    def prepareDiagonal(): Unit = {
      for (x <- two; y <- two) {
        prepareNormalDiagonal(x, y)
        prepareReverseDiagonal(5 - x, y)
      }
    }

    def prepareRow(x: Int, y: Int): Unit = {
      val seq = five map (i => (x + i, y))
      f(seq, num)
      num += 1
    }

    def prepareColumn(x: Int, y: Int): Unit = {
      val seq = five map (i => (x, y + i))
      f(seq, num)
      num += 1
    }

    def prepareNormalDiagonal(x: Int, y: Int): Unit = {
      val seq = five map (i => (x + i, y + i))
      f(seq, num)
      num += 1
    }

    def prepareReverseDiagonal(x: Int, y: Int): Unit = {
      val seq = five map (i => (x - i, y + i))
      f(seq, num)
      num += 1
    }

    prepareHorizontal()
    prepareVertical()
    prepareDiagonal()
  }


  def check(): Int = check(baseCounters)

  private def check(counters: Array[Counter]): Int = {
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

    return ret
  }

  def checkMove(m: Move, player: Int): Int = m match {
    case Position(x, y) => checkPosition(x, y, player)
    case Rotation(q, r) => checkRotate(q, r, player)
  }

  def checkPosition(x: Int, y: Int, player: Int): Int = {
    countField(baseCountersMap(x)(y), player)
    val value = check()
    unCountField(baseCountersMap(x)(y), player)
    return value
  }

  def checkRotate(q: Quadrant.Value, r: QRotation.Value, player: Int): Int = {
    val fastValue = r match {
      case QRotation.r90 => check(rotatedRightCounters(q.id))
      case QRotation.r270 => check(rotatedLeftCounters(q.id))
    }

    //    rotate(q, r, player)
    //    val value = check(baseCounters)
    //    unRotate(q, r, player)
    //
    //    if (value != fastValue) {
    //      error(s"rotation old value != fast value ($value != $fastValue)")
    //    }
    return fastValue
  }

  def isLegal(m: Move): Boolean = m match {
    case Position(x, y) => state(x)(y) == LightField.None
    case Rotation(q, r) => canRotate(q.id)
  }

  def position(x: Int, y: Int, player: Int): Unit = {
    state(x)(y) = player
    countField(baseCountersMap(x)(y), player)
    countField(rotatedCountersMap(x)(y), player)
    tick()
    freeFields -= 1
  }

  def unPosition(x: Int, y: Int, player: Int): Unit = {
    freeFields += 1
    unTick()
    unCountField(baseCountersMap(x)(y), player)
    unCountField(rotatedCountersMap(x)(y), player)
    state(x)(y) = LightField.None
  }

  def rotate(q: Quadrant.Value, rotation: QRotation.Value, player: Int): Unit = {
    val quadrant = q.id
    rotation match {
      case QRotation.r90 => rotateQuadrantLeft(quadrant)
      case QRotation.r270 => rotateQuadrantRight(quadrant)
    }
    quadrants(quadrant).rotate(rotation)
    tick()
  }

  def unRotate(q: Quadrant.Value, rotation: QRotation.Value, player: Int): Unit = {
    val quadrant = q.id
    unTick()
    quadrants(quadrant).unRotate(rotation)
    rotation match {
      case QRotation.r90 => rotateQuadrantRight(quadrant)
      case QRotation.r270 => rotateQuadrantLeft(quadrant)
    }
  }

  private def countField(counters: ArrayBuffer[Counter], f: Int): Unit = {
    if (f != LightField.None) {
      val countersLength = counters.length
      var i = 0
      while (i < countersLength) {
        val counter = counters(i)
        counter.add(f)
        i += 1
      }
    }
  }

  private def unCountField(counters: ArrayBuffer[Counter], f: Int): Unit = {
    if (f != LightField.None) {
      val countersLength = counters.length
      var i = 0
      while (i < countersLength) {
        val counter = counters(i)
        counter.sub(f)
        i += 1
      }
    }
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

  private def countBoard(): Unit = {
    var x = 0
    var y = 0
    while (x < 6) {
      y = 0
      while (y < 6) {
        val field = state(x)(y)
        countField(baseCountersMap(x)(y), field)
        countField(rotatedCountersMap(x)(y), field)
        y += 1
      }
      x += 1
    }
  }

  private def substitute(x1: Int, y1: Int, f2: Int): Unit = {
    val f1 = state(x1)(y1)

    if (f1 != f2) {
      unCountField(baseCountersMap(x1)(y1), f1)
      unCountField(rotatedCountersMap(x1)(y1), f1)
      state(x1)(y1) = f2
      countField(baseCountersMap(x1)(y1), f2)
      countField(rotatedCountersMap(x1)(y1), f2)
    }
  }

  private def rotateQuadrantLeft(quadrant: Int): Unit = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)
    //this is equivalent to rotate snake-like by 2 fields
    val t0 = state(x + 0)(y + 0)
    val t1 = state(x + 0)(y + 1)

    substitute(x + 0, y + 0, state(x + 0)(y + 2))
    substitute(x + 0, y + 1, state(x + 1)(y + 2))
    substitute(x + 0, y + 2, state(x + 2)(y + 2))
    substitute(x + 1, y + 2, state(x + 2)(y + 1))
    substitute(x + 2, y + 2, state(x + 2)(y + 0))
    substitute(x + 2, y + 1, state(x + 1)(y + 0))
    substitute(x + 2, y + 0, t0)
    substitute(x + 1, y + 0, t1)
  }

  private def rotateQuadrantRight(quadrant: Int): Unit = {
    val x = offsetX(quadrant)
    val y = offsetY(quadrant)
    //this is equivalent to rotate snake-like by 2 fields
    val t0 = state(x + 0)(y + 0)
    val t1 = state(x + 1)(y + 0)

    substitute(x + 0, y + 0, state(x + 2)(y + 0))
    substitute(x + 1, y + 0, state(x + 2)(y + 1))
    substitute(x + 2, y + 0, state(x + 2)(y + 2))
    substitute(x + 2, y + 1, state(x + 1)(y + 2))
    substitute(x + 2, y + 2, state(x + 0)(y + 2))
    substitute(x + 1, y + 2, state(x + 0)(y + 1))
    substitute(x + 0, y + 2, t0)
    substitute(x + 0, y + 1, t1)
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

  private def availableMoves(maximizing: Boolean): ArrayBuffer[ValuedMove] = {
    val moves = ArrayBuffer[ValuedMove]()
    val player = if (maximizing) LightField.X else LightField.O

    //since we are here, game is still on
    //taking a position can't cause draw, so adding is ok
    val valueBefore = check(baseCounters)
    var x = 0
    var y = 0
    while (x < 6) {
      y = 0
      while (y < 6) {
        if (state(x)(y) == 0) {
          val counters = baseCountersMap(x)(y)
          val countersLength = counters.length
          var won = false
          var value = valueBefore
          var i = 0
          while (i < countersLength) {
            val counter = counters(i)
            counter.add(player)
            val v = counter.getValue
            value += v
            if (v == Heuristics.winnerValue || v == -Heuristics.winnerValue) {
              won = true
            }
            i += 1
          }
          i = 0
          while (i < countersLength) {
            val counter = counters(i)
            counter.sub(player)
            i += 1
          }
          if (won) {
            value = if (maximizing) {
              Heuristics.winnerValue
            } else {
              -Heuristics.winnerValue
            }
          }
          moves.append(new ValuedMove(value, _cached_moves_positions(x)(y)))
        }
        y += 1
      }
      x += 1
    }

    //TODO: calculate value faster
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

  def findBestMove(maximizing: Boolean, randomize: Boolean): ValuedMove = {
    val moves = availableMoves(maximizing)
    var best = 0
    var bestNumber = 1
    var i = 1
    while (i < moves.length) {
      if (maximizing && moves(i).value > moves(best).value) {
        best = i
        bestNumber = 1
      } else if (!maximizing && moves(i).value < moves(best).value) {
        best = i
        bestNumber = 1
      } else if (moves(i).value == moves(best).value && randomize) {
        bestNumber += 1
        if (generator.nextInt(bestNumber) == 0) {
          best = i
        }
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

  def printCountersNum(countersMap: Array[Array[ArrayBuffer[Counter]]]): Unit = {
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
