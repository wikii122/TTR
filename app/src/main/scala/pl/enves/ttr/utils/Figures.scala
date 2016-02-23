package pl.enves.ttr.utils


case class Ray(P0: Array[Float], P1: Array[Float])

case class Triangle(V1: Array[Float], V2: Array[Float], V3: Array[Float])

object Triangle {
  def apply(): Triangle = {
    val v1 = Array(0.0f, 0.0f, 0.0f, 1.0f)
    val v2 = Array(0.0f, 0.0f, 0.0f, 1.0f)
    val v3 = Array(0.0f, 0.0f, 0.0f, 1.0f)
    return Triangle(v1, v2, v3)
  }
}
