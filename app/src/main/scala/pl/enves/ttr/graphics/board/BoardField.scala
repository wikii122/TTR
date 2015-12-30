package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.graphics.{MatrixStack, Resources}
import pl.enves.ttr.logic._

class BoardField(quadrant: Quadrant.Value, resources: Resources) extends Field(resources) {
  var value: Option[Player.Value] = None
  var winning = false

  var ring: Option[Int] = None
  var cross: Option[Int] = None
  var empty: Option[Int] = None

  //TODO: Load from settings
  var outerColor1: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var outerColor2: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var winnerOuterColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  override protected def onUpdateResources(screenRatio: Float): Unit = {
    super.onUpdateResources(screenRatio)
    ring = Some(resources.getTexture(TextureId.MaskRing))
    cross = Some(resources.getTexture(TextureId.MaskCross))
    empty = Some(resources.getTexture(TextureId.MaskEmpty))
  }

  override protected def onUpdateTheme(): Unit = {
    super.onUpdateTheme()
    outerColor1 = resources.getTheme.color1
    outerColor2 = resources.getTheme.color2
    winnerOuterColor = resources.getTheme.winner
  }

  override protected def onAnimate(dt: Float): Unit = {
    super.onAnimate(dt)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    super.onClick(clickX, clickY, viewport, mvMatrix, pMatrix)
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

  def defaultOuterColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => outerColor1
    case Quadrant.second => outerColor2
    case Quadrant.third => outerColor2
    case Quadrant.fourth => outerColor1
  }
}
