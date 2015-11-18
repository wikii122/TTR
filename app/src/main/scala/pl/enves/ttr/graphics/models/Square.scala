package pl.enves.ttr.graphics.models

import android.opengl.GLES20
import pl.enves.ttr.graphics.GeometryProvider
import pl.enves.ttr.graphics.geometry.Buffers

object Square extends GeometryProvider {
  private val coords = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    -0.5f, 0.5f, 0.0f,
    0.5f, 0.5f, 0.0f)

  private val texCoords = Array(
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f)

  override def getGeometry = createBaseGeometry(
    4,
    GLES20.GL_TRIANGLE_STRIP,
    new Buffers[Array[Float]](
      coords,
      texCoords
    )
  )
}