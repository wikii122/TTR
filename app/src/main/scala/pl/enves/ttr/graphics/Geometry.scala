package pl.enves.ttr.graphics

import android.opengl.GLES20

/**
 * Class to store buffers, arrays or VBOs, that belong to the same model
 */
case class Buffers[T](positions: T, texCoords: T)

/**
 * @param drawMode one of POINTS, LINE_STRIP, LINE_LOOP, LINES, TRIANGLE_STRIP, TRIANGLE_FAN, TRIANGLES
 */
case class GeometryData(drawMode: Int, buffers: Buffers[Array[Float]])

/**
 * @param numVertices number of vertices to draw
 */
case class Geometry(numVertices: Int, drawMode: Int, buffers: Buffers[Int]) {
  def draw(): Unit = {
    GLES20.glDrawArrays(drawMode, 0, numVertices)
  }
}
