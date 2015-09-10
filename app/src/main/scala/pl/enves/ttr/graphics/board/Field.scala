package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.themes.ColorId

class Field(resources: Resources) extends SceneObject with Logging {
  objectScale = Array(0.9f, 0.9f, 1.0f)

  var square: Option[Geometry] = None

  var maskShader: Option[MaskShader] = None

  var noColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  //TODO: Load from settings
  val illegalHighlightTime: Float = 1.0f

  var illegalHighlightTimeLeft: Float = 0.0f

  override protected def onUpdateResources(): Unit = {
    square = Some(resources.getGeometry(DefaultGeometryId.Square.toString))

    maskShader = Some(resources.getShader(ShaderId.Mask).asInstanceOf[MaskShader])
  }

  override protected def onUpdateTheme(): Unit = {
    noColor = resources.getTheme.rgba(ColorId.background, 0.0f) //To nicely fade-out on edges
  }

  override protected def onAnimate(dt: Float): Unit = {
    if (checkIllegal()) {
      illegalHighlightTimeLeft -= dt
      if (illegalHighlightTimeLeft < 0.0f) {
        illegalHighlightTimeLeft = 0.0f
      }
    }
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}

  def checkIllegal(): Boolean = {
    illegalHighlightTimeLeft > 0.0f
  }

  def discardIllegal(): Unit = {
    illegalHighlightTimeLeft = 0.0f
  }

  def setIllegal(): Unit = {
    illegalHighlightTimeLeft = illegalHighlightTime
  }
}
