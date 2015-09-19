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
}