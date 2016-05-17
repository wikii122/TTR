package pl.enves.ttr.utils.math

//TODO: Useful things
case class Vector2(x: Float, y: Float) {
  def toSeq2: Seq[Float] = Seq(x, y)
}

object Vector2 {
  def apply(): Vector2 = Vector2(0.0f, 0.0f)
}
