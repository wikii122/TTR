package pl.enves.ttr.graphics.board

import android.opengl.Matrix
import pl.enves.ttr.graphics.themes.{ColorId, Theme}
import pl.enves.ttr.graphics.{DefaultTextureId, Resources, MatrixStack}
import pl.enves.ttr.logic.{QRotation, Quadrant}

class ArrowField(quadrant: Quadrant.Value, rotation: QRotation.Value, resources: Resources) extends Field(resources) {

  var arrow: Option[Int] = None

  var outerColor1 = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var outerColor2 = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var illegalOuterColor = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var inactiveColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  def defaultArrowColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => outerColor1
    case Quadrant.second => outerColor2
    case Quadrant.third => outerColor2
    case Quadrant.fourth => outerColor1
  }

  var active = false

  override protected def onUpdateResources(): Unit = {
    super.onUpdateResources()
    rotation match {
      case QRotation.r90 => arrow = Some(resources.getTexture(DefaultTextureId.MaskArrowLeft.toString))
      case QRotation.r270 => arrow = Some(resources.getTexture(DefaultTextureId.MaskArrowRight.toString))
    }
  }

  override protected def onUpdateTheme(): Unit = {
    super.onUpdateTheme()
    outerColor1 = resources.getTheme.rgba(ColorId.outer1)
    outerColor2 = resources.getTheme.rgba(ColorId.outer2)
    illegalOuterColor = resources.getTheme.rgba(ColorId.outerIllegal)
    inactiveColor = resources.getTheme.rgba(ColorId.inactive)
  }

  override protected def onAnimate(dt: Float): Unit = {
    super.onAnimate(dt)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    super.onClick(clickX, clickY, viewport, mvMatrix, pMatrix)
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    val inner = if (active) {
      inactiveColor
    } else {
      defaultArrowColor(quadrant)
    }

    val outer = if (checkIllegal()) {
      illegalOuterColor
    } else {
      noColor
    }

    maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, inner, outer, arrow.get))
  }
}
