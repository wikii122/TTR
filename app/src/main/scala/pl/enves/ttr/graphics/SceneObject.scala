package pl.enves.ttr.graphics

import android.opengl.Matrix

import scala.collection.mutable

/**
 *
 */
trait SceneObject {
  protected val children: mutable.ListBuffer[SceneObject] = mutable.ListBuffer()

  protected var objectPosition = Array[Float](0.0f, 0.0f, 0.0f)
  protected var objectRotationAngle = 0.0f
  protected var objectRotation = Array[Float](0.0f, 0.0f, 1.0f)
  protected var objectScale = Array[Float](1.0f, 1.0f, 1.0f)

  protected var visible = true

  protected def onUpdateResources(): Unit

  protected def onUpdateTheme(): Unit

  protected def onAnimate(dt: Float): Unit

  protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit

  protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean

  def addChild(child: SceneObject): Unit = {
    children.append(child)
  }

  protected def transformToPosition(mvMatrix: MatrixStack): Unit = {
    Matrix.translateM(mvMatrix.get(), 0, objectPosition(0), objectPosition(1), objectPosition(2))
    Matrix.rotateM(mvMatrix.get(), 0, objectRotationAngle, objectRotation(0), objectRotation(1), objectRotation(2))
    Matrix.scaleM(mvMatrix.get(), 0, objectScale(0), objectScale(1), objectScale(2))
  }

  def updateResources(): Unit = {
    reset()
    //Allow children to setup first
    for (child <- children) {
      child.updateResources()
    }
    onUpdateResources()
  }

  def updateTheme(): Unit = {
    onUpdateTheme()
    for (child <- children) {
      child.updateTheme()
    }
  }

  def animate(dt: Float): Unit = {
    onAnimate(dt)
    for (child <- children) {
      child.animate(dt)
    }
  }

  def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    if (visible) {
      mvMatrix.push()
      transformToPosition(mvMatrix)
      onDraw(mvMatrix, pMatrix)
      for (child <- children) {
        child.draw(mvMatrix, pMatrix)
      }
      mvMatrix.pop()
    }
  }

  def click(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    mvMatrix.push()
    transformToPosition(mvMatrix)
    var result = false
    for (child <- children) {
      if (!result) {
        result = child.click(clickX, clickY, viewport, mvMatrix, pMatrix)
      }
    }
    if (!result) {
      result = onClick(clickX, clickY, viewport, mvMatrix, pMatrix)
    }
    mvMatrix.pop()
    return result
  }

  def scale(x: Float, y: Float, z: Float): Unit = {
    objectScale(0) *= x
    objectScale(1) *= y
    objectScale(2) *= z
  }

  def translate(x: Float, y: Float, z: Float): Unit = {
    objectPosition(0) += x
    objectPosition(1) += y
    objectPosition(2) += z
  }

  def rotate(a: Float): Unit = {
    objectRotationAngle += a
  }

  def rotation(a: Float, x: Float, y: Float, z: Float): Unit = {
    objectRotationAngle = a
    objectRotation(0) = x
    objectRotation(1) = y
    objectRotation(2) = z
  }

  def reset(): Unit = {
    objectPosition = Array[Float](0.0f, 0.0f, 0.0f)
    objectRotationAngle = 0.0f
    objectRotation = Array[Float](0.0f, 0.0f, 1.0f)
    objectScale = Array[Float](1.0f, 1.0f, 1.0f)
  }
}
