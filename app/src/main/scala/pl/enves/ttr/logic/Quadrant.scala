package pl.enves.ttr.logic

object Quadrant extends Enumeration {
  val size = 3

  val first, second, third, fourth = Value

  def offset(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0, 0)
    case Quadrant.second => (Quadrant.size, 0)
    case Quadrant.third => (0, Quadrant.size)
    case Quadrant.fourth => (Quadrant.size, Quadrant.size)
  }

  def apply(x: Int, y: Int) = if (y < Quadrant.size) {
    if (x < Quadrant.size) Quadrant.first else Quadrant.second
  } else {
    if (x < Quadrant.size) Quadrant.third else Quadrant.fourth
  }
}
