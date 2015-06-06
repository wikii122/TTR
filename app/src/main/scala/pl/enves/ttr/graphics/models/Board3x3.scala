package pl.enves.ttr.graphics.models

/**
 * Model of 1/4 of game board
 */
object Board3x3 {
  val coords = Array(

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

  val indices = Array[Short](
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
  val colors = Array(
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
}