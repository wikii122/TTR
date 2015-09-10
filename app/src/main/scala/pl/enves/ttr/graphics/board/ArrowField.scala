package pl.enves.ttr.graphics.board

import pl.enves.ttr.graphics.themes.ColorId
import pl.enves.ttr.graphics.{DefaultTextureId, Resources, MatrixStack}
import pl.enves.ttr.logic.{QRotation, Quadrant}

class ArrowField(quadrant: Quadrant.Value, rotation: QRotation.Value, resources: Resources) extends Field(resources) {

  var arrow: Option[Int] = None

  var colorActive = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var colorInactive = Array(0.0f, 0.0f, 0.0f, 0.0f)

  var active = false

  def defaultArrowColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => ColorId.outer1
    case Quadrant.second => ColorId.outer2
    case Quadrant.third => ColorId.outer2
    case Quadrant.fourth => ColorId.outer1
  }

  override protected def onUpdateResources(): Unit = {
    super.onUpdateResources()
    rotation match {
      case QRotation.r90 => arrow = Some(resources.getTexture(DefaultTextureId.MaskArrowLeft.toString))
      case QRotation.r270 => arrow = Some(resources.getTexture(DefaultTextureId.MaskArrowRight.toString))
    }
  }

  override protected def onUpdateTheme(): Unit = {
    super.onUpdateTheme()
    colorActive = resources.getTheme.rgba(defaultArrowColor(quadrant))
    colorInactive = resources.getTheme.rgba(defaultArrowColor(quadrant), 0.25f)
  }

  override protected def onAnimate(dt: Float): Unit = {
    super.onAnimate(dt)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    super.onClick(clickX, clickY, viewport, mvMatrix, pMatrix)
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val inner = if (active) {
      colorActive
    } else {
      colorInactive
    }

    maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, inner, noColor, arrow.get))
  }
}
