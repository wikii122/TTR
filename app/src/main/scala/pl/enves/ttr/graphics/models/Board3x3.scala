package pl.enves.ttr.graphics.models

import android.opengl.GLES20
import pl.enves.ttr.graphics.{BuffersData, ElementsGeometryData}

/**
 * Model of 1/4 of game board
 */
object Board3x3 {
  private val coords = Array(

    -0.5f, -0.5f, 0.0f, //Bottom Left
    0.5f, -0.5f, 0.0f, //Bottom Right
    0.5f, 0.5f, 0.0f, //Top Right
    -0.5f, 0.5f, 0.0f, //Top Left
    -1.0f/6.0f, -0.5f, 0.0f, //Bottom Center Left
    1.0f/6.0f, -0.5f, 0.0f, //Bottom Center Right
    -1.0f/6.0f, 0.5f, 0.0f, //Top Center Left
    1.0f/6.0f, 0.5f, 0.0f, //Top Center Right
    -0.5f, -1.0f/6.0f, 0.0f, //Left Center Bottom
    -0.5f, 1.0f/6.0f, 0.0f, //Left Center Top
    0.5f, -1.0f/6.0f, 0.0f, //Right Center Bottom
    0.5f, 1.0f/6.0f, 0.0f //Right Center Top
  )

  private val indices = Array[Short](
    //Outer
    0, 1,
    1, 2,
    2, 3,
    3, 0,
    //Inner
    4, 6,
    5, 7,
    8, 10,
    9, 11
  )

  private val colors = Array(
    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,
    1.0f, 0.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f
  )

  val board3x3Geometry = new ElementsGeometryData(
    indices.length,
    indices,
    GLES20.GL_LINES,
    new BuffersData(
      Some(coords),
      Some(colors),
      None,
      None
    )
  )
}