package pl.enves.ttr.graphics.geometry

/**
 * Rectangle of GL_TRIANGLES
 */
object Rectangle {
  def positionsCenterYTriangles(x: Float, y: Float, width: Float, height: Float): Array[Float] = {
    val w = width
    val h = height / 2.0f
    return Array(
      x - 0, y - h, 0.0f, //0
      x + w, y - h, 0.0f, //1
      x - 0, y + h, 0.0f, //2
      x - 0, y + h, 0.0f, //2
      x + w, y - h, 0.0f, //1
      x + w, y + h, 0.0f) //3
  }

  def texCoordinatesTriangles(x: Float, y: Float, w: Float, h: Float) = Array(
    x, y, //0
    x + w, y, //1
    x, y + h, //2
    x, y + h, //2
    x + w, y, //1
    x + w, y + h) //3
}
