package pl.enves.ttr.graphics.models

import android.opengl.GLES20
import pl.enves.ttr.graphics.{BuffersData, ArraysGeometryData}

object Square {
  private val coords = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    -0.5f, 0.5f, 0.0f,
    0.5f, 0.5f, 0.0f)

  private val numVertex = 4

  private val texCoords = Array(
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f)

  val squareGeometry = new ArraysGeometryData(
    numVertex,
    GLES20.GL_TRIANGLE_STRIP,
    new BuffersData(
      Some(coords),
      Some(texCoords)
    )
  )
}