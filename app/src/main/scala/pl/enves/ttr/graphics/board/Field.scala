package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics.geometry.{Geometry, GeometryId}
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.graphics.{MatrixStack, Resources, SceneObject}
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Triangle
import pl.enves.ttr.utils.themes.Theme

class Field(quadrant: Quadrant.Value)
  extends SceneObject with ColorManip {

  private[this] var value: Option[Player.Value] = None
  private[this] var winning = false

  private[this] var square: Option[Geometry] = None

  private[this] var maskShader: Option[MaskShader] = None

  private[this] var ring: Option[Int] = None
  private[this] var cross: Option[Int] = None
  private[this] var empty: Option[Int] = None

  private[this] var outerColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private[this] var winnerOuterColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  def setValue(v: Option[Player.Value]): Unit = {
    value = v
  }

  protected def getValue = value

  def setWinning(w: Boolean): Unit = {
    winning = w
  }

  override protected def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    square = Some(resources.getGeometry(GeometryId.Square))

    maskShader = Some(resources.getMaskShader)

    ring = Some(resources.getTexture(TextureId.MaskRing))
    cross = Some(resources.getTexture(TextureId.MaskCross))
    empty = Some(resources.getTexture(TextureId.MaskEmpty))
  }

  override protected def onUpdateTheme(theme: Theme): Unit = {
    outerColor = quadrant match {
      case Quadrant.first => theme.color1
      case Quadrant.second => theme.color2
      case Quadrant.third => theme.color2
      case Quadrant.fourth => theme.color1
    }
    winnerOuterColor = theme.winner
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val outer = if (winning) {
      winnerOuterColor
    } else {
      outerColor
    }

    if (value.isDefined) {
      value.get match {
        case Player.O => maskShader.get.draw(mvMatrix, pMatrix, square.get, outer, ring.get)
        case Player.X => maskShader.get.draw(mvMatrix, pMatrix, square.get, outer, cross.get)
      }
    } else {
      maskShader.get.draw(mvMatrix, pMatrix, square.get, outer, empty.get)
    }
  }

  override def getBoundingFigure: Array[Triangle] = if (square.isDefined) {
    square.get.getBoundingFigure
  } else {
    Array(Triangle())
  }
}
