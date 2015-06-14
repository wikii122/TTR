package pl.enves.ttr.utils

trait Vector3 {
  protected def dotProduct(u: Array[Float], v: Array[Float]): Float = u(0) * v(0) + u(1) * v(1) + u(2) * v(2)

  protected def sub(u: Array[Float], v: Array[Float]) = Array(
    u(0)-v(0),
    u(1)-v(1),
    u(2)-v(2)
  )

  protected def add(u: Array[Float], v: Array[Float]) = Array(
    u(0)+v(0),
    u(1)+v(1),
    u(2)+v(2)
  )

  protected def scale(r: Float, u: Array[Float]) = Array(
    u(0)*r,
    u(1)*r,
    u(2)*r
  )

  protected def crossProduct(u: Array[Float], v: Array[Float]) = Array(
    (u(1) * v(2)) - (u(2) * v(1)),
    (u(2) * v(0)) - (u(0) * v(2)),
    (u(0) * v(1)) - (u(1) * v(0))
  )

  protected def length(u: Array[Float]): Float =
    Math.abs(Math.sqrt((u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2)))).toFloat
}
