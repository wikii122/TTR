package pl.enves.ttr.graphics.transformations

import android.opengl.Matrix

class Translation(x: Float, y: Float, z: Float, enabled: Boolean) extends Transformation(enabled) {

  private var tx = x
  private var ty = y
  private var tz = z

  def setX(x: Float): Unit = tx = x

  def getX = tx

  def getDefaultX = x

  def setY(y: Float): Unit = ty = y

  def getY = ty

  def getDefaultY = y

  def setZ(z: Float): Unit = tz = z

  def getZ = tz

  def getDefaultZ = z

  override protected def onReset(): Unit = {
    tx = x
    ty = y
    tz = z
  }

  override protected def onTransform(matrix: Array[Float]): Unit = {
    Matrix.translateM(matrix, 0, tx, ty, tz)
  }
}
