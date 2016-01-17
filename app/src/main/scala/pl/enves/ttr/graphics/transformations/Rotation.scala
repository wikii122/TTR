package pl.enves.ttr.graphics.transformations

import android.opengl.Matrix

class Rotation(a: Float, x: Float, y: Float, z: Float, enabled: Boolean) extends Transformation(enabled) {

  private[this] var ra = a
  private[this] var rx = x
  private[this] var ry = y
  private[this] var rz = z

  def setA(a: Float): Unit = ra = a

  def getA = ra

  def getDefaultA = a

  def setX(x: Float): Unit = rx = x

  def getDefaultX = x

  def setY(y: Float): Unit = ry = y

  def getDefaultY = y

  def setZ(z: Float): Unit = rz = z

  def getDefaultZ = z

  override protected def onReset(): Unit = {
    ra = a
    rx = x
    ry = y
    rz = z
  }

  override protected def onTransform(matrix: Array[Float]): Unit = {
    Matrix.rotateM(matrix, 0, ra, rx, ry, rz)
  }
}
