package pl.enves.ttr.graphics.models

object Rectangle {
  def coords(width: Float, height: Float): Array[Float] = {
    val w = width / 2.0f
    val h = height / 2.0f
    return Array(
      -w, -h, 0.0f,
      w, -h, 0.0f,
      -w, h, 0.0f,
      w, h, 0.0f)
  }

  def coords(x: Float, y: Float, w: Float, h: Float): Array[Float] = Array(
    x, y, 0.0f,
    x + w, y, 0.0f,
    x, y + h, 0.0f,
    x + w, y + h, 0.0f)

  def texCoords(x: Float, y: Float, w: Float, h: Float) = Array(
    x, y,
    x + w, y,
    x, y + h,
    x + w, y + h)
}
