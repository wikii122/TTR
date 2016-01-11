package pl.enves.ttr.graphics.transformations

import android.opengl.Matrix

class Scale(x: Float, y: Float, z: Float, enabled: Boolean) extends Transformation(enabled) {

  private var sx = x
  private var sy = y
  private var sz = z

  def setX(x: Float): Unit = sx = x

  def getX = sx

  def getDefaultX = x

  def setY(y: Float): Unit = sy = y

  def getY = sy

  def getDefaultY = y

  def setZ(z: Float): Unit = sz = z

  def getZ = sz

  def getDefaultZ = z

  override protected def onReset(): Unit = {
    sx = x
    sy = y
    sz = z
  }

  override protected def onTransform(matrix: Array[Float]): Unit = {
    Matrix.scaleM(matrix, 0, sx, sy, sz)
  }
}
