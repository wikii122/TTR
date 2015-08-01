package pl.enves.ttr.graphics.board

trait Coordinates {
  def logicToDisplay(a: Int): Float = (2 * a - 5) / 2.0f

  def displayToLogic(a: Float): Int = {
    val i = Math.floor(Math.abs(a)).toInt
    return if (a >= 0) 3 + i else 2 - i
  }
}
