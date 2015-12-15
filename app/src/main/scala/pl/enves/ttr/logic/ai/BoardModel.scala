package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{QRotation, Quadrant}

import scala.collection.mutable.ArrayBuffer

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

  val fieldNeighbours = Array.fill[Int] (6, 6) { 0 }

  private val countersMap = Array.fill(6, 6) {
    ArrayBuffer[Counter]()
  }
  private val counters = prepareCounters()

  //Their purpose is reducing GC pressure by reducing fresh allocations
  private val _cached_moves_positions = Array.tabulate(6, 6) {
    (x: Int, y: Int) => new LightPosition(x, y)
  }

  private val _cached_moves_rotations_left = Array(
    new LightRotation(0, QRotation.r90),
    new LightRotation(1, QRotation.r90),
    new LightRotation(2, QRotation.r90),
    new LightRotation(3, QRotation.r90)
  )

  private val _cached_moves_rotations_right = Array(
    new LightRotation(0, QRotation.r270),
    new LightRotation(1, QRotation.r270),
    new LightRotation(2, QRotation.r270),
    new LightRotation(3, QRotation.r270)
  )

  //printCountersNum()
  //printCountersCoordinates()

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
      counter.setCoordinates(i, x + i, y)
    })
  }

  private def prepareColumnCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x)(y + i).append(counter)
      counter.setCoordinates(i, x, y + i)
    })
  }

  private def prepareNormalDiagonalCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x + i)(y + i).append(counter)
      counter.setCoordinates(i, x + i, y + i)
    })
  }

  private def prepareReverseDiagonalCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
    val counter = new Counter()
    buf.append(counter)
    five foreach (i => {
      countersMap(x - i)(y + i).append(counter)
      counter.setCoordinates(i, x - i, y + i)
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

    return ret
  }

  def move(x: Int, y: Int, player: Int): Unit = {
    state(x)(y) = player
    countField(x, y)
    tick()
    freeFields -= 1
  }

  def unMove(x: Int, y: Int, player: Int): Unit = {
    freeFields += 1
    unTick()
    unCountField(x, y)
    state(x)(y) = LightField.None
  }

  def rotate(quadrant: Int, rotation: QRotation.Value, player: Int): Unit = {
    unCountQuadrantForRotation(quadrant)
    rotation match {
      case QRotation.r90 => rotateQuadrantLeft(quadrant)
      case QRotation.r270 => rotateQuadrantRight(quadrant)
    }
    countQuadrantForRotation(quadrant)
    quadrants(quadrant).rotate(rotation)
    tick()
  }

  def unRotate(quadrant: Int, rotation: QRotation.Value, player: Int): Unit = {
    unTick()
    quadrants(quadrant).unRotate(rotation)
    unCountQuadrantForRotation(quadrant)
    rotation match {
      case QRotation.r90 => rotateQuadrantRight(quadrant)
      case QRotation.r270 => rotateQuadrantLeft(quadrant)
    }
    countQuadrantForRotation(quadrant)
  }

  private def countField(x: Int, y: Int): Unit = {
    val f = state(x)(y)
    if (f != LightField.None) {
      val counters = countersMap(x)(y)
      val countersLength = counters.length
      var i = 0
      var j = 0
      while (i < countersLength) {
        val counter = counters(i)
        counter.add(f)

        j = 0
        while (j < 5) {
          // field can be its own neighbour, no check
          fieldNeighbours(counter.getCoordinateX(j))(counter.getCoordinateY(j)) += 1
          j += 1
        }
        i += 1
      }
    }
  }

  private def unCountField(x: Int, y: Int): Unit = {
    val f = state(x)(y)
    if (f != LightField.None) {
      val counters = countersMap(x)(y)
      val countersLength = counters.length
      var i = 0
      var j = 0
      while (i < countersLength) {
        val counter = counters(i)
        counter.sub(f)

        j = 0
        while (j < 5) {
          // field can be its own neighbour, no check
          fieldNeighbours(counter.getCoordinateX(j))(counter.getCoordinateY(j)) -= 1
          j += 1
        }
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
    quadrants(0).tickCooldown()
    quadrants(1).tickCooldown()
    quadrants(2).tickCooldown()
    quadrants(3).tickCooldown()
  }

  def unTick(): Unit = {
    quadrants(0).unTickCooldown()
    quadrants(1).unTickCooldown()
    quadrants(2).unTickCooldown()
    quadrants(3).unTickCooldown()
  }

  case class ValuedPosition(value: Int, position: LightPosition) extends Ordered[ValuedPosition] {
    def compare(that: ValuedPosition) = that.value - value
  }

  //TODO: more heuristic optimizations
  //Like building symmetrical sets of figures
  def availableMoves(): ArrayBuffer[LightMove] = {
    val moves: ArrayBuffer[LightMove] = ArrayBuffer[LightMove]()
    if (freeFields == 36) {
      //TODO: Pick one at random
      moves.append(_cached_moves_positions(1)(1))
      return moves
    }

    var x = 0
    var y = 0
    var neighbours = 0
    var sum = 0
    val valuedPositions: ArrayBuffer[ValuedPosition] = ArrayBuffer[ValuedPosition]()
    while (x < 6) {
      y = 0
      while (y < 6) {
        if (state(x)(y) == 0) {
          //Don't bother fields that lays in empty sequences
          neighbours = fieldNeighbours(x)(y)
          if (neighbours > 0) {
            sum += neighbours
            valuedPositions.append(new ValuedPosition(neighbours, _cached_moves_positions(x)(y)))
          }
        }
        y += 1
      }
      x += 1
    }

    //Fields with more neighbours are more likely to be important
    //Checking them first increases possibility of alpha-beta cutoff
    val sortedValuedPositions = valuedPositions.toArray
    scala.util.Sorting.quickSort(sortedValuedPositions)
    val positionsLength: Int = sortedValuedPositions.length

    val median = if (positionsLength > 0) {
      if (positionsLength % 2 == 0) {
        (sortedValuedPositions(positionsLength / 2).value + sortedValuedPositions((positionsLength / 2) + 1).value) / 2
      } else {
        sortedValuedPositions((positionsLength / 2) + 1).value
      }
    } else {
      0
    }

    val average = sum / positionsLength

    var i = 0
    //warning - omitting positions with small value may cause idiocy
    positionsChoosing match {
      case PositionsChoosing.Reasonable =>
        while (i < positionsLength) {
          moves.append(sortedValuedPositions(i).position)
          i += 1
        }
      case PositionsChoosing.GEMedian =>
        while (i < positionsLength && sortedValuedPositions(i).value >= median) {
          moves.append(sortedValuedPositions(i).position)
          i += 1
        }
      case PositionsChoosing.GEAverage =>
        while (i < positionsLength && sortedValuedPositions(i).value >= average) {
          moves.append(sortedValuedPositions(i).position)
          i += 1
        }
      case PositionsChoosing.Max8 =>
        while (i < positionsLength && i < 8) {
          moves.append(sortedValuedPositions(i).position)
          i += 1
        }
      case PositionsChoosing.Max12 =>
        while (i < positionsLength && i < 12) {
          moves.append(sortedValuedPositions(i).position)
          i += 1
        }
      case PositionsChoosing.Max16 =>
        while (i < positionsLength && i < 16) {
          moves.append(sortedValuedPositions(i).position)
          i += 1
        }
    }
    //Generally it is better to take a new field than rotate
    //So append rotations at the end
    var quadrant = 0
    while (quadrant < 4) {
      if (canRotate(quadrant) && !isRotationImmune(quadrant)) {
        moves.append(_cached_moves_rotations_left(quadrant))
        moves.append(_cached_moves_rotations_right(quadrant))
      }
      quadrant += 1
    }
    return moves
  }

  def finished = freeFields == 0

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

  def printCountersCoordinates(): Unit = {
    for (x <- six)
      for (y <- six) {
        val counters = countersMap(x)(y)
        for (c <- counters) {
          val c0 = c.getCoordinates(0)
          val c1 = c.getCoordinates(1)
          val c2 = c.getCoordinates(2)
          val c3 = c.getCoordinates(3)
          val c4 = c.getCoordinates(4)
          log(s"X: $x, Y: $y: $c0 $c1 $c2 $c3 $c4")
        }
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

  def printNeighbours(): Unit = {
    for (y <- six) {
      val s0 = fieldNeighbours(0)(5 - y)
      val s1 = fieldNeighbours(1)(5 - y)
      val s2 = fieldNeighbours(2)(5 - y)
      val s3 = fieldNeighbours(3)(5 - y)
      val s4 = fieldNeighbours(4)(5 - y)
      val s5 = fieldNeighbours(5)(5 - y)
      log(s"neighbours: $s0 $s1 $s2 $s3 $s4 $s5")
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
