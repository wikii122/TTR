package pl.enves.ttr.logic

object QRotation extends Enumeration(0) {
  val r0, r90, r180, r270 = Value

  implicit class QRotationValueExtension(rotation: QRotation.Value) {
    def add(other: QRotation.Value) = QRotation((rotation.id + other.id) % 4)

    def sub(other: QRotation.Value) = QRotation((rotation.id - other.id + 4) % 4)
  }
}

object Quadrant extends Enumeration {
  val size = 3

  val first, second, third, fourth = Value

  def offset(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0, 0)
    case Quadrant.second => (Quadrant.size, 0)
    case Quadrant.third => (0, Quadrant.size)
    case Quadrant.fourth => (Quadrant.size, Quadrant.size)
  }
}