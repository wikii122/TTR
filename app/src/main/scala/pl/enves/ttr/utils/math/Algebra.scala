package pl.enves.ttr.utils.math

import javax.microedition.khronos.opengles.GL10

import android.opengl.GLU

trait Algebra {

  class UnProjectException(msg: String) extends RuntimeException(msg)

  def unProjectMatrices(mvMatrix: Array[Float],
                        pMatrix: Array[Float],
                        clickX: Float,
                        clickY: Float,
                        viewport: Array[Int]): Ray = {
    val temp1 = new Array[Float](4)
    val temp2 = new Array[Float](4)

    val result1 = GLU.gluUnProject(clickX, clickY, 1.0f, mvMatrix, 0, pMatrix, 0, viewport, 0, temp1, 0)
    val result2 = GLU.gluUnProject(clickX, clickY, 0.0f, mvMatrix, 0, pMatrix, 0, viewport, 0, temp2, 0)

    if (result1 != GL10.GL_TRUE || result2 != GL10.GL_TRUE) {
      throw new UnProjectException("ModelView or Projection Matrix cannot be inverted")
    }

    val near = Vector3(temp1(0), temp1(1), temp1(2)) * (1.0f / temp1(3))

    val far = Vector3(temp2(0), temp2(1), temp2(2)) * (1.0f / temp2(3))

    return Ray(near, far)
  }

  /**
   * https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
   */
  def isRayIntersectingTriangle(triangle: Triangle, ray: Ray): Boolean = {
    val EPSILON = 0.0001f

    //ray direction
    val D = ray.p1 - ray.p0

    //Find vectors for two edges sharing V1
    val e1 = triangle.p1 - triangle.p0 //Edge1
    val e2 = triangle.p2 - triangle.p0 //Edge2

    //Begin calculating determinant - also used to calculate u parameter
    val P = D cross e2

    //if determinant is near zero, ray lies in plane of triangle
    val det = e1 dot P

    //NOT CULLING
    if (det > -EPSILON && det < EPSILON) return false

    val inv_det = 1.0f / det

    //calculate distance from V1 to ray origin
    val T = ray.p0 - triangle.p0

    //Calculate u parameter and test bound
    val u = (T dot P) * inv_det

    //The intersection lies outside of the triangle
    if (u < 0.0f || u > 1.0f) return false

    //Prepare to test v parameter
    val Q = T cross e1

    //Calculate V parameter and test bound
    val v = (D dot Q) * inv_det

    //The intersection lies outside of the triangle
    if (v < 0.0f || u + v > 1.0f) return false

    val t = (e2 dot Q) * inv_det

    if (t > EPSILON) {
      //ray intersection
      return true
    }

    // No hit, no win
    return false
  }
}
