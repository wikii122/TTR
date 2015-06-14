package pl.enves.ttr.graphics.models

object Square {
  val coords = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    -0.5f, 0.5f, 0.0f,
    0.5f, 0.5f, 0.0f)

  val numVertex = 4

  val colors = Array(
    0.5f, 0.0f, 0.0f, 1.0f,
    0.5f, 0.0f, 0.0f, 1.0f,
    0.5f, 0.0f, 0.0f, 1.0f,
    0.5f, 0.0f, 0.0f, 1.0f)

  val texCoords = Array(
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f)
}
