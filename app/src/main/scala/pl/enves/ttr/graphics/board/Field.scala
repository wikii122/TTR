package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.shaders.MaskShader

class Field(resources: Resources) extends SceneObject with Logging with ColorManip {
  objectScale = Array(0.9f, 0.9f, 1.0f)

  var square: Option[AbstractGeometry] = None

  var maskShader: Option[MaskShader] = None

  var noColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  //TODO: Load from settings
  protected var shakeTime: Float = 1.0f //seconds

  protected var shakeAmplitude = 15.0f

  protected var shakeFrequency = 5.0f //Hz

  private var shaken = false

  private var shakeTimeElapsed: Float = 0.0f

  private var notStirred = 0.0f

  override protected def onUpdateResources(): Unit = {
    square = Some(resources.getGeometry(GeometryId.Square))

    maskShader = Some(resources.getMaskShader)
  }

  override protected def onUpdateTheme(): Unit = {
    val noColor: ColorArray = colorTransparent(resources.getTheme.background, 0.0f)
  }

  override protected def onAnimate(dt: Float): Unit = {
    if (shaken) {
      val s = shakeAmplitude * Math.sin(shakeTimeElapsed * shakeFrequency * 2 * Math.PI).toFloat
      objectRotationAngle = notStirred + s * Math.sin((shakeTimeElapsed / shakeTime) * Math.PI).toFloat //Apply Ease In And Ease Out

      shakeTimeElapsed += dt
      if (shakeTimeElapsed >= shakeTime) {
        discardIllegal()
      }
    }
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}

  def discardIllegal(): Unit = {
    if (shaken) {
      shaken = false
      objectRotationAngle = notStirred
    }
  }

  def setIllegal(): Unit = {
    if (!shaken) {
      shaken = true
      notStirred = objectRotationAngle
    }
    shakeTimeElapsed = 0.0f
  }
}
