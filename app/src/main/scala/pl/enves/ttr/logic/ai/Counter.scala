package pl.enves.ttr.logic.ai

class Counter() {
  private var nx: Int = 0
  private var no: Int = 0

  def add(player: Int): Unit = {
    if (player == LightField.X) {
      nx += 1
    }
    if (player == LightField.O) {
      no += 1
    }
  }

  def sub(player: Int): Unit = {
    if (player == LightField.X) {
      nx -= 1
    }
    if (player == LightField.O) {
      no -= 1
    }
  }

  def getTaken: Int = nx + no

  def getValue = calculateValue()

  private def calculateValue(): Int = {
    val x = nx
    val o = no
    return if (x == 5) Heuristics.winnerValue
    else if (o == 5) -Heuristics.winnerValue
    else if (o == 0 && x > 0) x * x * x
    else if (x == 0 && o > 0) -(o * o * o)
    else 0
  }
}
