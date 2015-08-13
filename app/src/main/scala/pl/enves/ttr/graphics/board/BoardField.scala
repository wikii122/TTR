package pl.enves.ttr.graphics.board

import pl.enves.ttr.graphics.themes.{ColorId, Theme}
import pl.enves.ttr.graphics.{Resources, DefaultTextureId, MatrixStack}
import pl.enves.ttr.logic._

class BoardField(quadrant: Quadrant.Value, resources: Resources) extends Field(resources) {
  var value: Option[Player.Value] = None
  var winning = false

  var ring: Option[Int] = None
  var cross: Option[Int] = None
  var empty: Option[Int] = None

  //TODO: Load from settings
  var crossColor = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var ringColor = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var outerColor1 = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var outerColor2 = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var winnerOuterColor = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var illegalOuterColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  override protected def onUpdateResources(): Unit = {
    super.onUpdateResources()
    ring = Some(resources.getTexture(DefaultTextureId.MaskRing.toString))
    cross = Some(resources.getTexture(DefaultTextureId.MaskCross.toString))
    empty = Some(resources.getTexture(DefaultTextureId.MaskEmpty.toString))
  }

  override protected def onUpdateTheme(): Unit = {
    super.onUpdateTheme()
    crossColor = resources.getTheme.rgba(ColorId.cross)
    ringColor = resources.getTheme.rgba(ColorId.ring)
    outerColor1 = resources.getTheme.rgba(ColorId.outer1)
    outerColor2 = resources.getTheme.rgba(ColorId.outer2)
    winnerOuterColor = resources.getTheme.rgba(ColorId.outerWinner)
    illegalOuterColor = resources.getTheme.rgba(ColorId.outerIllegal)
  }

  override protected def onAnimate(dt: Float): Unit = {
    super.onAnimate(dt)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    super.onClick(clickX, clickY, viewport, mvMatrix, pMatrix)
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val outer = if (checkIllegal()) {
      illegalOuterColor
    } else {
      if (winning) {
        winnerOuterColor
      } else {
        defaultOuterColor(quadrant)
      }
    }

    if (value.isDefined) {
      value.get match {
        case Player.O => maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, ringColor, outer, ring.get))
        case Player.X => maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, crossColor, outer, cross.get))
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
