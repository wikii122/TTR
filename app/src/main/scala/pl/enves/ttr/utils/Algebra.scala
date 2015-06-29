package pl.enves.ttr.utils

import javax.microedition.khronos.opengles.GL10

import android.opengl.GLU

trait Algebra extends Vector3 {

  class UnProjectException(msg: String) extends RuntimeException(msg)

  class IntersectException(msg: String) extends RuntimeException(msg)

  def unProjectMatrices(mvMatrix: Array[Float],
                        pMatrix: Array[Float],
                        clickX: Float,
                        clickY: Float,
                        viewport: Array[Int]): (Array[Float], Array[Float]) = {
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

    return (near, far)
  }

  def intersectRayAndXYPlane(P0: Array[Float], P1: Array[Float]): Array[Float] = {
    val I = new Array[Float](3)
    val planePoint = Array(0.0f, 0.0f, 0.0f)
    val planeNormal = Array(0.0f, 0.0f, 1.0f)

    var r, a, b: Float = 0.0f

    val SMALL_NUM = 0.0001f

    val rayDirection = sub(P1, P0)
    val w0 = sub(P0, planePoint)
    a = -dotProduct(planeNormal, w0)
    b = dotProduct(planeNormal, rayDirection)
    if (Math.abs(b) < SMALL_NUM) {
      //if (a == 0) {
      // throw new IntersectException("Ray lies in plane")
      //}
      throw new IntersectException("Ray is parallel to plane")
    }

    // Check if specified segment intersects with plane
    r = a / b
    if (r < 0.0f || r > 1.0f) {
      throw new IntersectException("Ray segment doesn't intersect with plane")
    }

    // Get intersection point
    val temp = scale(r, rayDirection)
    val tI = add(P0, temp)

    tI.indices foreach {
      i => I(i) = tI(i)
    }

    return I
  }
}
