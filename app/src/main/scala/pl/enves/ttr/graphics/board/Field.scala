package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics.animations.{FieldRotation, Shake}
import pl.enves.ttr.graphics.geometry.{Geometry, GeometryId}
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.graphics.{MatrixStack, Resources, SceneObject}
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Triangle

class Field(game: Game, quadrant: Quadrant.Value, resources: Resources)
  extends SceneObject with ColorManip with Illegal {

  private[this] var value: Option[Player.Value] = None
  private[this] var winning = false

  private[this] var square: Option[Geometry] = None

  private[this] var maskShader: Option[MaskShader] = None

  private[this] var noColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  private[this] var shakeAnimation: Option[Shake] = None
  private[this] var rotationAnimation: Option[FieldRotation] = None

  private[this] var ring: Option[Int] = None
  private[this] var cross: Option[Int] = None
  private[this] var empty: Option[Int] = None

  private[this] var outerColor1: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private[this] var outerColor2: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private[this] var winnerOuterColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  def setValue(v: Option[Player.Value], animateChange: Boolean): Unit = {
    if (animateChange && v != value) {
      shakeAnimation.get.stop()
      rotationAnimation.get.start()
    }
    value = v
  }

  def setWinning(w: Boolean): Unit = {
    winning = w
  }

  override protected def onUpdateResources(screenRatio: Float): Unit = {
    square = Some(resources.getGeometry(GeometryId.Square))

    maskShader = Some(resources.getMaskShader)

    val scale = addScale(1.0f, 1.0f, 1.0f, false)
    val rotation = addRotation(0.0f, 0.0f, 0.0f, 1.0f, false)

    shakeAnimation = Some(new Shake(1.0f, rotation, 15.0f, 5.0f))
    rotationAnimation = Some(new FieldRotation(1.0f, rotation, scale))


    ring = Some(resources.getTexture(TextureId.MaskRing))
    cross = Some(resources.getTexture(TextureId.MaskCross))
    empty = Some(resources.getTexture(TextureId.MaskEmpty))
  }

  override protected def onUpdateTheme(): Unit = {
    noColor = colorTransparent(resources.getTheme.background, 0.0f)
    outerColor1 = resources.getTheme.color1
    outerColor2 = resources.getTheme.color2
    winnerOuterColor = resources.getTheme.winner
  }

  override protected def onAnimate(dt: Float): Unit = {
    shakeAnimation.get.animate(dt)
    rotationAnimation.get.animate(dt)
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val outer = if (winning) {
      winnerOuterColor
    } else {
      defaultOuterColor(quadrant)
    }

    if (value.isDefined) {
      value.get match {
        case Player.O => maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, noColor, outer, ring.get))
        case Player.X => maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, noColor, outer, cross.get))
      }
    } else {
      maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, noColor, outer, empty.get))
    }
  }

  override def getBoundingFigure: Array[Triangle] = square.get.getBoundingFigure

  private def defaultOuterColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => outerColor1
    case Quadrant.second => outerColor2
    case Quadrant.third => outerColor2
    case Quadrant.fourth => outerColor1
  }

  def stopAnimations(): Unit = {
    rotationAnimation.get.stop()
    shakeAnimation.get.stop()
  }

  override def discardIllegal(): Unit = {
    shakeAnimation.get.stop()
  }

  override def setIllegal(): Unit = {
    rotationAnimation.get.stop()
    shakeAnimation.get.start()
  }
}
