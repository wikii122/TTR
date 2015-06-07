package pl.enves.ttr.utils

object Vector3 {

  def dotProduct(u: Array[Float], v: Array[Float]): Float = {
    return u(0) * v(0) + u(1) * v(1) + u(2) * v(2)
  }

  def sub(res: Array[Float], u: Array[Float], v: Array[Float]) = {
    res(0) = u(0)-v(0)
    res(1) = u(1)-v(1)
    res(2) = u(2)-v(2)
  }

  def add(res: Array[Float], u: Array[Float], v: Array[Float]) = {
    res(0) = u(0)+v(0)
    res(1) = u(1)+v(1)
    res(2) = u(2)+v(2)
  }

  def scale(res: Array[Float], r: Float, u: Array[Float]) = {
    res(0) = u(0)*r
    res(1) = u(1)*r
    res(2) = u(2)*r
  }

  def crossProduct(res: Array[Float], u: Array[Float], v: Array[Float]) = {
    res(0) = (u(1) * v(2)) - (u(2) * v(1))
    res(1) = (u(2) * v(0)) - (u(0) * v(2))
    res(2) = (u(0) * v(1)) - (u(1) * v(0))
  }

  def length(u: Array[Float]): Float = {
    return Math.abs(Math.sqrt((u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2)))).toFloat
  }
}
