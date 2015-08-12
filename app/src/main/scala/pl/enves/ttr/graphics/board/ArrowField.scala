package pl.enves.ttr.graphics.board

import android.opengl.Matrix
import pl.enves.ttr.graphics.{DefaultTextureId, Resources, MatrixStack}
import pl.enves.ttr.logic.{QRotation, Quadrant}

class ArrowField(quadrant: Quadrant.Value, rotation: QRotation.Value, resources: Resources) extends Field(resources){

  var arrow: Option[Int] = None

  //TODO: Load from settings
  var outerColor1 = Array(179.0f / 255.0f, 179.0f / 255.0f, 179.0f / 255.0f, 1.0f)
  var outerColor2 = Array(255.0f / 255.0f, 255.0f / 255.0f, 255.0f / 255.0f, 1.0f)
  var illegalOuterColor = Array(179.0f / 255.0f, 0.0f / 255.0f, 0.0f / 255.0f, 1.0f)
  var inactiveColor = Array(55.0f / 255.0f, 55.0f / 255.0f, 55.0f / 255.0f, 1.0f)

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
