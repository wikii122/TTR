package pl.enves.ttr.graphics.models

object Triangle {
  val coords = Array(// in counterclockwise order:
    0.0f, 0.5f, 0.0f, // top
    -0.5f, -0.5f, 0.0f, // bottom left
    0.5f, -0.5f, 0.0f) // bottom right

  val colors = Array(// in counterclockwise order:
    1.0f, 0.0f, 0.0f, 1.0f, // top
    0.0f, 1.0f, 0.0f, 1.0f, // bottom left
    0.0f, 0.0f, 1.0f, 1.0f) // bottom right

  val numVertex = 3

  val texCoords = Array(
    0.5f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f)
}