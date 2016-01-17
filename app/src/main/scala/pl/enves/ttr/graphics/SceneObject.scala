package pl.enves.ttr.graphics

import pl.enves.ttr.graphics.transformations.{Rotation, Scale, Transformation, Translation}

import scala.collection.mutable

trait SceneObject {
  private[this] val children = mutable.ListBuffer[SceneObject]()

  private[this] val transformations = mutable.ListBuffer[Transformation]()

  private[this] var visible = true

  protected def onUpdateResources(screenRatio: Float): Unit

  protected def onUpdateTheme(): Unit

  protected def onAnimate(dt: Float): Unit

  protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit

  protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean

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

  def setVisible(v: Boolean) = visible = v
}
