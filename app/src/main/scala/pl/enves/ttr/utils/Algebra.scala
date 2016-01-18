package pl.enves.ttr.utils

import javax.microedition.khronos.opengles.GL10

import android.opengl.GLU

trait Algebra extends Vector3 {

  class UnProjectException(msg: String) extends RuntimeException(msg)

  def unProjectMatrices(mvMatrix: Array[Float],
                        pMatrix: Array[Float],
                        clickX: Float,
                        clickY: Float,
                        viewport: Array[Int]): Ray = {
    val temp1 = new Array[Float](4)
    val temp2 = new Array[Float](4)
    val near = new Array[Float](3)
    val far = new Array[Float](3)

    val result1 = GLU.gluUnProject(clickX, clickY, 1.0f, mvMatrix, 0, pMatrix, 0, viewport, 0, temp1, 0)
    val result2 = GLU.gluUnProject(clickX, clickY, 0.0f, mvMatrix, 0, pMatrix, 0, viewport, 0, temp2, 0)

    if (result1 != GL10.GL_TRUE || result2 != GL10.GL_TRUE) {
      throw new UnProjectException("ModelView or Projection Matrix cannot be inverted")
    }

    near(0) = temp1(0) / temp1(3)
    near(1) = temp1(1) / temp1(3)
    near(2) = temp1(2) / temp1(3)

    far(0) = temp2(0) / temp2(3)
    far(1) = temp2(1) / temp2(3)
    far(2) = temp2(2) / temp2(3)

    return Ray(near, far)
  }

  /**
   * https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
   */
  def isRayIntersectingTriangle(triangle: Triangle, ray: Ray): Boolean = {
    val EPSILON = 0.0001f

    //ray direction
    val D = sub(ray.P1, ray.P0)

    //Find vectors for two edges sharing V1
    val e1 = sub(triangle.V2, triangle.V1) //Edge1
    val e2 = sub(triangle.V3, triangle.V1) //Edge2

    //Begin calculating determinant - also used to calculate u parameter
    val P = crossProduct(D, e2)

    //if determinant is near zero, ray lies in plane of triangle
    val det = dotProduct(e1, P)

    //NOT CULLING
    if (det > -EPSILON && det < EPSILON) return false

    val inv_det = 1.0f / det

    //calculate distance from V1 to ray origin
    val T = sub(ray.P0, triangle.V1)

    //Calculate u parameter and test bound
    val u = dotProduct(T, P) * inv_det

    //The intersection lies outside of the triangle
    if (u < 0.0f || u > 1.0f) return false

    //Prepare to test v parameter
    val Q = crossProduct(T, e1)

    //Calculate V parameter and test bound
    val v = dotProduct(D, Q) * inv_det

    //The intersection lies outside of the triangle
    if (v < 0.0f || u + v > 1.0f) return false

    val t = dotProduct(e2, Q) * inv_det

    if (t > EPSILON) {
      //ray intersection
      return true
    }

    // No hit, no win
    return false
  }
}
