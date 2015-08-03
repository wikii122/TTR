package pl.enves.ttr.graphics

import android.opengl.Matrix

import scala.collection.mutable

/**
 *
 */
trait SceneObject {
  private val children: mutable.ListBuffer[SceneObject] = mutable.ListBuffer()

  var objectPosition = Array[Float](0.0f, 0.0f, 0.0f)
  var objectRotationAngle = 0.0f
  var objectRotation = Array[Float](0.0f, 0.0f, 1.0f)
  var objectScale = Array[Float](1.0f, 1.0f, 1.0f)

  protected def onUpdateResources(): Unit

  protected def onAnimate(dt: Float): Unit

  protected def onDraw(): Unit

  protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean

  def addChild(child: SceneObject): Unit = {
    children.append(child)
  }

  def transformToPosition(): Unit = {
    Matrix.translateM(MVMatrix(), 0, objectPosition(0), objectPosition(1), objectPosition(2))
    Matrix.rotateM(MVMatrix(), 0, objectRotationAngle, objectRotation(0), objectRotation(1), objectRotation(2))
    Matrix.scaleM(MVMatrix(), 0, objectScale(0), objectScale(1), objectScale(2))
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

  def draw(): Unit = {
    MVMatrix.push()
    transformToPosition()
    onDraw()
    for (child <- children) {
      child.draw()
    }
    MVMatrix.pop()
  }

  def click(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean = {
    MVMatrix.push()
    transformToPosition()
    var result = false
    for (child <- children) {
      if (!result) {
        result = child.click(clickX, clickY, viewport)
      }
    }
    if (!result) {
      result = onClick(clickX, clickY, viewport)
    }
    MVMatrix.pop()
    return result
  }
}
