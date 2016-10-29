package pl.enves.ttr.graphics.geometry

import pl.enves.ttr.utils.math._

/**
 * Rectangle made with two Triangles
 */
object Rectangle {
  def apply(leftTop: Vector3, rightBottom: Vector3): List[Triangle] = {

    val p0 = leftTop
    val p1 = Vector3(rightBottom.x, leftTop.y, leftTop.z)
    val p2 = Vector3(leftTop.x, rightBottom.y, rightBottom.z)
    val p3 = rightBottom

    return List(Triangle(p0, p1, p2), Triangle(p2, p1, p3))
  }
}
