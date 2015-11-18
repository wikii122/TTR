package pl.enves.ttr.graphics

import android.opengl.GLES20
import pl.enves.ttr.graphics.geometry.Buffers

/**
 * @param drawMode one of POINTS, LINE_STRIP, LINE_LOOP, LINES, TRIANGLE_STRIP, TRIANGLE_FAN, TRIANGLES
 * @param numVertices number of vertices to draw
 */
abstract class AbstractGeometry(numVertices: Int, drawMode: Int, buffers: Buffers[Int]) {
  def getBuffers = buffers

  final val PositionSize = 3
  final val TexCoordSize = 2

  def draw(): Unit = {
    GLES20.glDrawArrays(drawMode, 0, numVertices)
  }
}
