package pl.enves.ttr.graphics.models

import android.opengl.GLES20
import pl.enves.ttr.graphics.{Buffers, GeometryData}

object Square {
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

  val squareGeometry = new GeometryData(
    GLES20.GL_TRIANGLE_STRIP,
    new Buffers[Array[Float]](
      coords,
      texCoords
    )
  )
}