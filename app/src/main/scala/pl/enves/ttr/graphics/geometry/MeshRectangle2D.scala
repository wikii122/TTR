package pl.enves.ttr.graphics.geometry

import android.graphics.RectF
import pl.enves.ttr.utils.math._

/**
 * Rectangle made with two Triangles on XY plane
 */
object MeshRectangle2D {
  def apply(): List[MeshTriangle] = MeshRectangle2D(
    new RectF(-0.5f, -0.5f, 0.5f, 0.5f),
    new RectF(0.0f, 0.0f, 1.0f, 1.0f)
  )

  def apply(position: RectF, texCoord: RectF): List[MeshTriangle] = {
    val p0 = Vector3(position.left, position.top, 0.0f)
    val p1 = Vector3(position.right, position.top, 0.0f)
    val p2 = Vector3(position.left, position.bottom, 0.0f)
    val p3 = Vector3(position.right, position.bottom, 0.0f)

    val t0 = Vector2(texCoord.left, texCoord.top)
    val t1 = Vector2(texCoord.right, texCoord.top)
    val t2 = Vector2(texCoord.left, texCoord.bottom)
    val t3 = Vector2(texCoord.right, texCoord.bottom)

    val v0 = MeshVertex(p0, t0)
    val v1 = MeshVertex(p1, t1)
    val v2 = MeshVertex(p2, t2)
    val v3 = MeshVertex(p3, t3)

    return List(MeshTriangle(v0, v1, v2), MeshTriangle(v2, v1, v3))
  }
}
