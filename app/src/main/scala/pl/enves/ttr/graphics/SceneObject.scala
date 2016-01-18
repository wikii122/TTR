package pl.enves.ttr.graphics

import android.opengl.Matrix
import pl.enves.ttr.graphics.transformations.{Rotation, Scale, Transformation, Translation}
import pl.enves.ttr.utils.{Algebra, Ray, Triangle}

import scala.collection.mutable

trait SceneObject extends Algebra {
  private[this] val children = mutable.ListBuffer[SceneObject]()

  private[this] val transformations = mutable.ListBuffer[Transformation]()

  private[this] var visible = true

  protected def onUpdateResources(screenRatio: Float): Unit = {}

  protected def onUpdateTheme(): Unit = {}

  protected def onAnimate(dt: Float): Unit = {}

  protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}

  protected def onClick(): Unit = {}

  protected def getBoundingFigure: Array[Triangle] = Array[Triangle]()

  def addChild(child: SceneObject): Unit = {
    children.append(child)
  }

  def addTransformation(transformation: Transformation): Unit = {
    transformations.append(transformation)
  }

  def removeTransformation(transformation: Transformation): Unit = {
    val i = transformations.indexOf(transformation)
    if (i != -1) {
      transformations.remove(i)
    }
  }

  def addScale(x: Float, y: Float, z: Float, enabled: Boolean): Scale = {
    val scale = new Scale(x, y, z, enabled)
    transformations.append(scale)
    return scale
  }

  def addTranslation(x: Float, y: Float, z: Float, enabled: Boolean): Translation = {
    val translation = new Translation(x, y, z, enabled)
    transformations.append(translation)
    return translation
  }

  def addRotation(a: Float, x: Float, y: Float, z: Float, enabled: Boolean): Rotation = {
    val rotation = new Rotation(a, x, y, z, enabled)
    transformations.append(rotation)
    return rotation
  }

  def reset(): Unit = {
    transformations.clear()
    for (child <- children) {
      child.reset()
    }
  }

  protected def transformToPosition(mvMatrix: MatrixStack): Unit = {
    for (transformation <- transformations) {
      transformation.transform(mvMatrix.get())
    }
  }

  def updateResources(screenRatio: Float): Unit = {
    onUpdateResources(screenRatio)
    for (child <- children) {
      child.updateResources(screenRatio)
    }
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

  def click(eyeSpaceRay: Ray, mvMatrix: MatrixStack): Boolean = {
    mvMatrix.push()
    transformToPosition(mvMatrix)
    val boundingFigure = getBoundingFigure

    var result = if (boundingFigure.length != 0) {

      val p1 = Array(0.0f, 0.0f, 0.0f, 1.0f)
      val p2 = Array(0.0f, 0.0f, 0.0f, 1.0f)
      val p3 = Array(0.0f, 0.0f, 0.0f, 1.0f)

      var intersect = false
      var triangle = 0
      while (triangle < boundingFigure.length && !intersect) {
        Matrix.multiplyMV(p1, 0, mvMatrix.get(), 0, boundingFigure(triangle).V1, 0)
        Matrix.multiplyMV(p2, 0, mvMatrix.get(), 0, boundingFigure(triangle).V2, 0)
        Matrix.multiplyMV(p3, 0, mvMatrix.get(), 0, boundingFigure(triangle).V3, 0)
        val eyeSpaceTriangle = Triangle(p1, p2, p3)

        intersect |= isRayIntersectingTriangle(eyeSpaceTriangle, eyeSpaceRay)
        triangle += 1
      }
      if (intersect) {
        onClick()
      }

      intersect
    } else {
      false
    }

    for (child <- children) {
      result |= child.click(eyeSpaceRay, mvMatrix)
    }

    mvMatrix.pop()
    return result
  }

  def setVisible(v: Boolean) = visible = v
}
