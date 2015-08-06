package pl.enves.ttr.graphics

import android.opengl.Matrix

import scala.collection.mutable

/**
 *
 */
trait SceneObject {
  protected val children: mutable.ListBuffer[SceneObject] = mutable.ListBuffer()

  var objectPosition = Array[Float](0.0f, 0.0f, 0.0f)
  var objectRotationAngle = 0.0f
  var objectRotation = Array[Float](0.0f, 0.0f, 1.0f)
  var objectScale = Array[Float](1.0f, 1.0f, 1.0f)

  protected def onUpdateResources(): Unit

  protected def onAnimate(dt: Float): Unit

  protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit

  protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean

  def addChild(child: SceneObject): Unit = {
    children.append(child)
  }

  def transformToPosition(mvMatrix: MatrixStack): Unit = {
    Matrix.translateM(mvMatrix.get(), 0, objectPosition(0), objectPosition(1), objectPosition(2))
    Matrix.rotateM(mvMatrix.get(), 0, objectRotationAngle, objectRotation(0), objectRotation(1), objectRotation(2))
    Matrix.scaleM(mvMatrix.get(), 0, objectScale(0), objectScale(1), objectScale(2))
  }

  def updateResources(): Unit = {
    onUpdateResources()
    for (child <- children) {
      child.updateResources()
    }
  }

  def animate(dt: Float): Unit = {
    onAnimate(dt)
    for (child <- children) {
      child.animate(dt)
    }
  }

  def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    mvMatrix.push()
    transformToPosition(mvMatrix)
    onDraw(mvMatrix, pMatrix)
    for (child <- children) {
      child.draw(mvMatrix, pMatrix)
    }
    mvMatrix.pop()
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
}
