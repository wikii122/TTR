package pl.enves.ttr.graphics.geometry

import android.opengl.GLES20

class SquareGeometry extends Geometry {
  private[this] val coords = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    -0.5f, 0.5f, 0.0f,
    0.5f, 0.5f, 0.0f)

  private[this] val texCoords = Array(
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f)

  private[this] val buffersGpu = new Buffers[Int](
    createFloatBuffer(coords),
    createFloatBuffer(unflipY(texCoords))
  )

  override def getNumVertices: Int = 4

  override def getDrawMode: Int = GLES20.GL_TRIANGLE_STRIP

  override def getBuffers: Buffers[Int] = buffersGpu
}