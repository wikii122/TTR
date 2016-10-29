package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics.animations.Shake
import pl.enves.ttr.graphics.geometry.{Geometry, GeometryId}
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.graphics.{MatrixStack, Resources, SceneObject}
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.math.Triangle
import pl.enves.ttr.utils.themes.Theme

class Arrow(makeMove: Move => Unit, quadrant: Quadrant.Value, rotation: QRotation.Value)
  extends SceneObject with ColorManip with Illegal {

  private[this] var square: Option[Geometry] = None

  private[this] var maskShader: Option[MaskShader] = None

  private[this] var shakeAnimation: Option[Shake] = None

  private[this] var arrow: Option[Int] = None

  private[this] var colorActive: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private[this] var colorInactive: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  private[this] var active = false

  def setActive(a: Boolean): Unit = {
    active = a
  }

  private def defaultArrowColor(quadrant: Quadrant.Value, theme: Theme) = quadrant match {
    case Quadrant.first => theme.color1
    case Quadrant.second => theme.color2
    case Quadrant.third => theme.color2
    case Quadrant.fourth => theme.color1
  }

  private def arrowRotation(quadrant: Quadrant.Value): Float = quadrant match {
    case Quadrant.first => 0.0f
    case Quadrant.second => 90.0f
    case Quadrant.third => 270.0f
    case Quadrant.fourth => 180.0f
  }

  override protected def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    square = Some(resources.getGeometry(GeometryId.Square))

    maskShader = Some(resources.getMaskShader)

    arrow = rotation match {
      case QRotation.r90 => Some(resources.getTexture(TextureId.MaskArrowRight))
      case QRotation.r270 => Some(resources.getTexture(TextureId.MaskArrowLeft))
    }

    val rot = addRotation(arrowRotation(quadrant), 0.0f, 0.0f, 1.0f, true)

    shakeAnimation = Some(new Shake(1.0f, rot, 30.0f, 5.0f))
  }

  override protected def onUpdateTheme(theme: Theme): Unit = {
    colorActive = defaultArrowColor(quadrant, theme)
    colorInactive = colorTransparent(defaultArrowColor(quadrant, theme), 0.3f)
  }

  override protected def onSyncState(game: Game): Unit = {
    active = game.canRotate(quadrant)
  }

  override protected def onAnimate(dt: Float): Unit = {
    shakeAnimation.get.animate(dt)
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val inner = if (active) {
      colorActive
    } else {
      colorInactive
    }

    maskShader.get.draw(mvMatrix, pMatrix, square.get, inner, arrow.get)
  }

  override def onClick(): Unit = {
    try {
      val move = new Rotation(quadrant, rotation)
      makeMove(move)
      discardIllegal()
    } catch {
      case e: RotationLocked =>
        setIllegal()
      case e: BoardLocked =>
        setIllegal()
    }
  }

  override def getBoundingFigure: List[Triangle] = square map (_.boundingFigure) getOrElse Nil

  override def discardIllegal(): Unit = {
    shakeAnimation.get.stop()
  }

  override def setIllegal(): Unit = {
    shakeAnimation.get.start()
  }
}
