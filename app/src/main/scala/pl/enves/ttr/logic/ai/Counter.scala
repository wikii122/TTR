package pl.enves.ttr.logic.ai

class Counter(fieldImportance: Array[Array[Int]]) {
  private var nx: Int = 0
  private var no: Int = 0

  private val coordinates = Array.fill[Int](10) { 0 }

  def add(player: Int): Unit = {
    val oldImportance = getImportance
    if (player == LightField.X) {
      nx += 1
    }
    if (player == LightField.O) {
      no += 1
    }
    adjustImportances(getImportance - oldImportance)
  }

  def sub(player: Int): Unit = {
    val oldImportance = getImportance
    if (player == LightField.X) {
      nx -= 1
    }
    if (player == LightField.O) {
      no -= 1
    }
    adjustImportances(getImportance - oldImportance)
  }

  def getTaken: Int = nx + no

  def getImportance: Int = {
    val x = nx
    val o = no
    return if (o == 0 && x > 0) x * x * x
    else if (x == 0 && o > 0) o * o * o
    else 0
  }

  def adjustImportances(diff: Int): Unit = {
    var i = 0
    while(i < 5) {
      fieldImportance(getCoordinateX(i))(getCoordinateY(i)) += diff
      i += 1
    }
  }

  def getValue: Int = {
    val x = nx
    val o = no
    return if (x == 5) Heuristics.winnerValue
    else if (o == 5) -Heuristics.winnerValue
    else if (o == 0 && x > 0) x * x * x
    else if (x == 0 && o > 0) -(o * o * o)
    else 0
  }

  def setCoordinates(i: Int, x: Int, y: Int): Unit = {
    coordinates(2 * i) = x
    coordinates(2 * i + 1) = y
  }

  def getCoordinateX(i: Int): Int = coordinates(2 * i)

  def getCoordinateY(i: Int): Int = coordinates(2 * i + 1)

  def getCoordinates(i: Int): (Int, Int) = (
    coordinates(2 * i),
    coordinates(2 * i + 1)
    )
}
