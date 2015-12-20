//package pl.enves.ttr.logic.ai
//
//import pl.enves.androidx.Logging
//import pl.enves.ttr.logic.{QRotation, Quadrant}
//
//import scala.collection.mutable.ArrayBuffer
//
//class ForwardBoard extends Logging {
//
//  private val five = for (x <- 0 until 5) yield x
//  private val six = for (x <- 0 until 6) yield x
//  private val two = for (x <- 0 until 2) yield x
//
//  val state = Array.fill[Int] (6, 6) { LightField.None }
//
//  private val countersMap = Array.fill(6, 6) {
//    ArrayBuffer[Counter]()
//  }
//  private val counters = prepareCounters()
//
//  private def prepareCounters(): Array[Counter] = {
//    val buf = ArrayBuffer[Counter]()
//    prepareHorizontalCounters(buf)
//    prepareVerticalCounters(buf)
//    prepareDiagonalCounters(buf)
//    return buf.toArray
//  }
//
//  private def prepareHorizontalCounters(buf: ArrayBuffer[Counter]): Unit = {
//    for (x <- two; y <- six) {
//      prepareRowCounter(x, y, buf)
//    }
//  }
//
//  private def prepareVerticalCounters(buf: ArrayBuffer[Counter]): Unit = {
//    for (x <- six; y <- two) {
//      prepareColumnCounter(x, y, buf)
//    }
//  }
//
//  private def prepareDiagonalCounters(buf: ArrayBuffer[Counter]): Unit = {
//    for (x <- two; y <- two) {
//      prepareNormalDiagonalCounter(x, y, buf)
//      prepareReverseDiagonalCounter(5 - x, y, buf)
//    }
//  }
//
//  private def prepareRowCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
//    val counter = new Counter(fieldImportance)
//    buf.append(counter)
//    five foreach (i => {
//      countersMap(x + i)(y).append(counter)
//      counter.setCoordinates(i, x + i, y)
//    })
//  }
//
//  private def prepareColumnCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
//    val counter = new Counter(fieldImportance)
//    buf.append(counter)
//    five foreach (i => {
//      countersMap(x)(y + i).append(counter)
//      counter.setCoordinates(i, x, y + i)
//    })
//  }
//
//  private def prepareNormalDiagonalCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
//    val counter = new Counter(fieldImportance)
//    buf.append(counter)
//    five foreach (i => {
//      countersMap(x + i)(y + i).append(counter)
//      counter.setCoordinates(i, x + i, y + i)
//    })
//  }
//
//  private def prepareReverseDiagonalCounter(x: Int, y: Int, buf: ArrayBuffer[Counter]): Unit = {
//    val counter = new Counter(fieldImportance)
//    buf.append(counter)
//    five foreach (i => {
//      countersMap(x - i)(y + i).append(counter)
//      counter.setCoordinates(i, x - i, y + i)
//    })
//  }
//
//  def check(): Int = {
//    var sum = 0
//    var winX = 0
//    var winO = 0
//    var i = 0
//    val countersLength = counters.length
//    while (i < countersLength) {
//      val v = counters(i).getValue
//      if (v == Heuristics.winnerValue) {
//        winX += 1
//      } else if (v == -Heuristics.winnerValue) {
//        winO += 1
//      } else {
//        sum += v
//      }
//      i += 1
//    }
//
//    val ret = if (winX == 0 && winO == 0) sum
//    else if (winX > 0 && winO == 0) Heuristics.winnerValue
//    else if (winO > 0 && winX == 0) -Heuristics.winnerValue
//    else 0 //draw
//
//    return ret
//  }
//
//  def move(x: Int, y: Int, player: Int): Unit = {
//    state(x)(y) = player
//    countField(x, y)
//  }
//
//  def unMove(x: Int, y: Int, player: Int): Unit = {
//    unCountField(x, y)
//    state(x)(y) = LightField.None
//  }
//
//  def rotate(quadrant: Int, rotation: QRotation.Value, player: Int): Unit = {
//    unCountQuadrantForRotation(quadrant)
//    rotation match {
//      case QRotation.r90 => rotateQuadrantLeft(quadrant)
//      case QRotation.r270 => rotateQuadrantRight(quadrant)
//    }
//    countQuadrantForRotation(quadrant)
//  }
//
//  def unRotate(quadrant: Int, rotation: QRotation.Value, player: Int): Unit = {
//    unCountQuadrantForRotation(quadrant)
//    rotation match {
//      case QRotation.r90 => rotateQuadrantRight(quadrant)
//      case QRotation.r270 => rotateQuadrantLeft(quadrant)
//    }
//    countQuadrantForRotation(quadrant)
//  }
//
//  private def countField(x: Int, y: Int): Unit = {
//    val f = state(x)(y)
//    if (f != LightField.None) {
//      val counters = countersMap(x)(y)
//      val countersLength = counters.length
//      var i = 0
//      var j = 0
//      while (i < countersLength) {
//        val counter = counters(i)
//        counter.add(f)
//        i += 1
//      }
//    }
//  }
//
//  private def unCountField(x: Int, y: Int): Unit = {
//    val f = state(x)(y)
//    if (f != LightField.None) {
//      val counters = countersMap(x)(y)
//      val countersLength = counters.length
//      var i = 0
//      var j = 0
//      while (i < countersLength) {
//        val counter = counters(i)
//        counter.sub(f)
//        i += 1
//      }
//    }
//  }
//
//  def offsetX(quadrant: Int) = quadrant match {
//    case 0 => 0
//    case 1 => Quadrant.size
//    case 2 => 0
//    case 3 => Quadrant.size
//  }
//
//  def offsetY(quadrant: Int) = quadrant match {
//    case 0 => 0
//    case 1 => 0
//    case 2 => Quadrant.size
//    case 3 => Quadrant.size
//  }
//
//  private def countQuadrantForRotation(quadrant: Int): Unit = {
//    val x = offsetX(quadrant)
//    val y = offsetY(quadrant)
//    //only quadrant centers don't change
//    countField(x + 0, y + 0)
//    countField(x + 0, y + 1)
//    countField(x + 0, y + 2)
//    countField(x + 1, y + 0)
//    countField(x + 1, y + 2)
//    countField(x + 2, y + 0)
//    countField(x + 2, y + 1)
//    countField(x + 2, y + 2)
//  }
//
//  private def unCountQuadrantForRotation(quadrant: Int): Unit = {
//    val x = offsetX(quadrant)
//    val y = offsetY(quadrant)
//    unCountField(x + 0, y + 0)
//    unCountField(x + 0, y + 1)
//    unCountField(x + 0, y + 2)
//    unCountField(x + 1, y + 0)
//    unCountField(x + 1, y + 2)
//    unCountField(x + 2, y + 0)
//    unCountField(x + 2, y + 1)
//    unCountField(x + 2, y + 2)
//  }
//
//  private def countBoard(): Unit = {
//    var x = 0
//    var y = 0
//    while (x < 6) {
//      y = 0
//      while (y < 6) {
//        countField(x, y)
//        y += 1
//      }
//      x += 1
//    }
//  }
//
//  private def rotateQuadrantLeft(quadrant: Int): Unit = {
//    val x = offsetX(quadrant)
//    val y = offsetY(quadrant)
//    //this is equivalent to rotate snake-like by 2 fields
//    val t0 = state(x + 0)(y + 0)
//    val t1 = state(x + 0)(y + 1)
//
//    state(x + 0)(y + 0) = state(x + 0)(y + 2)
//    state(x + 0)(y + 1) = state(x + 1)(y + 2)
//    state(x + 0)(y + 2) = state(x + 2)(y + 2)
//    state(x + 1)(y + 2) = state(x + 2)(y + 1)
//    state(x + 2)(y + 2) = state(x + 2)(y + 0)
//    state(x + 2)(y + 1) = state(x + 1)(y + 0)
//    state(x + 2)(y + 0) = t0
//    state(x + 1)(y + 0) = t1
//  }
//
//  private def rotateQuadrantRight(quadrant: Int): Unit = {
//    val x = offsetX(quadrant)
//    val y = offsetY(quadrant)
//    //this is equivalent to rotate snake-like by 2 fields
//    val t0 = state(x + 0)(y + 0)
//    val t1 = state(x + 1)(y + 0)
//
//    state(x + 0)(y + 0) = state(x + 2)(y + 0)
//    state(x + 1)(y + 0) = state(x + 2)(y + 1)
//    state(x + 2)(y + 0) = state(x + 2)(y + 2)
//    state(x + 2)(y + 1) = state(x + 1)(y + 2)
//    state(x + 2)(y + 2) = state(x + 0)(y + 2)
//    state(x + 1)(y + 2) = state(x + 0)(y + 1)
//    state(x + 0)(y + 2) = t0
//    state(x + 0)(y + 1) = t1
//  }
//
//  def printState(): Unit = {
//    def c(s: Int) = s match {
//      case LightField.X => "X"
//      case LightField.O => "O"
//      case _ => "#"
//    }
//    for (y <- six) {
//      val s0 = c(state(0)(5 - y))
//      val s1 = c(state(1)(5 - y))
//      val s2 = c(state(2)(5 - y))
//      val s3 = c(state(3)(5 - y))
//      val s4 = c(state(4)(5 - y))
//      val s5 = c(state(5)(5 - y))
//      log(s"state: $s0 $s1 $s2 $s3 $s4 $s5")
//    }
//  }
//
//}
//
//object ForwardBoard {
//  def apply() = new ForwardBoard()
//
//  def apply(from: BoardModel): ForwardBoard = {
//    val board = new ForwardBoard()
//
//    for (i <- 0 until 2 * Quadrant.size;
//         j <- 0 until 2 * Quadrant.size
//    ) board.state(i)(j) = from.state(j)(i)
//
//    board.countBoard()
//
//    return board
//  }
//}
