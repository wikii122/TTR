package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics.animations.Shake
import pl.enves.ttr.graphics.geometry.{Geometry, GeometryId}
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.graphics.{MatrixStack, Resources, SceneObject}
import pl.enves.ttr.logic.{QRotation, Quadrant}

class Arrow(quadrant: Quadrant.Value, rotation: QRotation.Value, resources: Resources)
  extends SceneObject with ColorManip with Illegal {

  private[this] var square: Option[Geometry] = None

  private[this] var maskShader: Option[MaskShader] = None

  private[this] var shakeAnimation: Option[Shake] = None

  private[this] var arrow: Option[Int] = None

  private[this] var noColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private[this] var colorActive: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private[this] var colorInactive: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  private[this] var active = false

  def setActive(a: Boolean): Unit = {
    active = a
  }

  private def defaultArrowColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => resources.getTheme.color1
    case Quadrant.second => resources.getTheme.color2
    case Quadrant.third => resources.getTheme.color2
    case Quadrant.fourth => resources.getTheme.color1
  }

  private def arrowRotation(quadrant: Quadrant.Value): Float = quadrant match {
    case Quadrant.first => 0.0f
    case Quadrant.second => 90.0f
    case Quadrant.third => 270.0f
    case Quadrant.fourth => 180.0f
  }

  override protected def onUpdateResources(screenRatio: Float): Unit = {
    square = Some(resources.getGeometry(GeometryId.Square))

    maskShader = Some(resources.getMaskShader)

    arrow = rotation match {
      case QRotation.r90 => Some(resources.getTexture(TextureId.MaskArrowLeft))
      case QRotation.r270 => Some(resources.getTexture(TextureId.MaskArrowRight))
    }

    val rot = addRotation(arrowRotation(quadrant), 0.0f, 0.0f, 1.0f, true)

    shakeAnimation = Some(new Shake(1.0f, rot, 30.0f, 5.0f))
  }

  override protected def onUpdateTheme(): Unit = {
    noColor = colorTransparent(resources.getTheme.background, 0.0f)
    colorActive = defaultArrowColor(quadrant)
    colorInactive = colorTransparent(defaultArrowColor(quadrant), 0.3f)
  }

  override protected def onAnimate(dt: Float): Unit = {
    shakeAnimation.get.animate(dt)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val inner = if (active) {
      colorActive
    } else {
      colorInactive
    }

    maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, inner, noColor, arrow.get))
  }

  override def discardIllegal(): Unit = {
    shakeAnimation.get.stop()
  }

  override def setIllegal(): Unit = {
    shakeAnimation.get.start()
  }
}
