package pl.enves.ttr.graphics

import android.opengl.Matrix
import pl.enves.ttr.graphics.transformations.{Rotation, Scale, Transformation, Translation}
import pl.enves.ttr.logic.Game
import pl.enves.ttr.utils.math.{Algebra, Ray, Triangle, Vector3}
import pl.enves.ttr.utils.themes.Theme

import scala.collection.mutable

trait SceneObject extends Algebra {
  private[this] val children = mutable.ArrayBuffer[SceneObject]()

  private[this] val transformations = mutable.ArrayBuffer[Transformation]()

  private[this] var visible = true

  protected def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {}

  protected def onAfterUpdateResources(resources: Resources, screenRatio: Float): Unit = {}

  protected def onUpdateTheme(theme: Theme): Unit = {}

  protected def onSyncState(game: Game): Unit = {}

  protected def onAnimate(dt: Float): Unit = {}

  protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}

  protected def onClick(): Unit = {}

  protected def getBoundingFigure: List[Triangle] = Nil

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
    val size = children.size
    var i = 0
    while (i < size) {
      children(i).reset()
      i += 1
    }
  }

  protected def transformToPosition(mvMatrix: MatrixStack): Unit = {
    val size = transformations.size
    var i = 0
    while (i < size) {
      transformations(i).transform(mvMatrix.get())
      i += 1
    }
  }

  def updateResources(resources: Resources, screenRatio: Float): Unit = {
    onUpdateResources(resources, screenRatio)
    val size = children.size
    var i = 0
    while (i < size) {
      children(i).updateResources(resources, screenRatio)
      i += 1
    }
    onAfterUpdateResources(resources, screenRatio)
  }

  def updateTheme(theme: Theme): Unit = {
    onUpdateTheme(theme)
    val size = children.size
    var i = 0
    while (i < size) {
      children(i).updateTheme(theme)
      i += 1
    }
  }

  def syncState(game: Game): Unit = {
    onSyncState(game)
    val size = children.size
    var i = 0
    while (i < size) {
      children(i).syncState(game)
      i += 1
    }
  }

  def animate(dt: Float): Unit = {
    onAnimate(dt)
    val size = children.size
    var i = 0
    while (i < size) {
      children(i).animate(dt)
      i += 1
    }
  }

  def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    if (visible) {
      mvMatrix.push()
      transformToPosition(mvMatrix)
      onDraw(mvMatrix, pMatrix)
      val size = children.size
      var i = 0
      while (i < size) {
        children(i).draw(mvMatrix, pMatrix)
        i += 1
      }
      mvMatrix.pop()
    }
  }

  def click(eyeSpaceRay: Ray, mvMatrix: MatrixStack): Boolean = {
    mvMatrix.push()
    transformToPosition(mvMatrix)
    val boundingFigure = getBoundingFigure

    var result = if (boundingFigure.nonEmpty) {

      val p1 = Array(0.0f, 0.0f, 0.0f, 1.0f)
      val p2 = Array(0.0f, 0.0f, 0.0f, 1.0f)
      val p3 = Array(0.0f, 0.0f, 0.0f, 1.0f)

      var intersect = false
      var triangle = 0
      while (triangle < boundingFigure.length && !intersect) {
        //TODO: Optimize memory usage
        Matrix.multiplyMV(p1, 0, mvMatrix.get(), 0, boundingFigure(triangle).p0.toArray4, 0)
        Matrix.multiplyMV(p2, 0, mvMatrix.get(), 0, boundingFigure(triangle).p1.toArray4, 0)
        Matrix.multiplyMV(p3, 0, mvMatrix.get(), 0, boundingFigure(triangle).p2.toArray4, 0)
        val eyeSpaceTriangle = Triangle(Vector3(p1), Vector3(p2), Vector3(p3))

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

    val size = children.size
    var i = 0
    while (i < size) {
      result |= children(i).click(eyeSpaceRay, mvMatrix)
      i += 1
    }

    mvMatrix.pop()
    return result
  }

  def setVisible(v: Boolean) = visible = v
}
