package pl.enves.ttr.utils.math

case class Vector3(x: Float, y: Float, z: Float) {
  def toSeq3 = Seq(x, y, z)

  def toArray4 = Array(x, y, z, 1.0f)

  def length(): Float =
    Math.abs(Math.sqrt(x * x) + (y * y) + (z * z)).toFloat

  def +(that: Vector3) = Vector3(
    this.x + that.x,
    this.y + that.y,
    this.z + that.z
  )

  def -(that: Vector3) = Vector3(
    this.x - that.x,
    this.y - that.y,
    this.z - that.z
  )

  def *(a: Float) = Vector3(
    this.x * a,
    this.y * a,
    this.z * a
  )

  def cross(that: Vector3) = Vector3(
    (this.y * that.z) - (this.z * that.y),
    (this.z * that.x) - (this.x * that.z),
    (this.x * that.y) - (this.y * that.x)
  )

  def dot(that: Vector3): Float =
    this.x * that.x + this.y * that.y + this.z * that.z
}

object Vector3 {
  def apply(): Vector3 = Vector3(0.0f, 0.0f, 0.0f)

  def apply(v: Array[Float]): Vector3 = Vector3(v(0), v(1), v(2))
}
