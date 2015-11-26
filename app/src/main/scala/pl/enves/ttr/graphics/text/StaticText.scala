package pl.enves.ttr.graphics.text

import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.{GeometryId, TextGeometry}
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.texture.TextureId

class StaticText(resources: Resources, geometryId: GeometryId.Value, textureId: TextureId.Value, maxW: Float, maxH: Float)
  extends Logging with SceneObject {

  private var textColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  private var backgroundColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  private var texture: Option[Int] = None
  private var geometry: Option[AbstractGeometry] = None
  private var maskShader: Option[MaskShader] = None

  override def onUpdateResources(screenRatio: Float): Unit = {
    texture = Some(resources.getTexture(textureId))
    geometry = Some(resources.getGeometry(geometryId))
    maskShader = Some(resources.getMaskShader)

    val textGeometry = resources.getGeometry(geometryId).asInstanceOf[TextGeometry]

    var scaleW, scaleH = 1.0f
    if (textGeometry.width > maxW) {
      scaleW = maxW / textGeometry.width
    }
    if (textGeometry.height > maxH) {
      scaleH = maxH / textGeometry.height
    }

    val s = Math.min(scaleW, scaleH)
    scale(s, s, 1.0f)
  }

  override protected def onUpdateTheme(): Unit = {}

  def setTextColor(color: ColorArray): Unit = {
    textColor = color
  }

  def setTextBackground(color: ColorArray): Unit = {
    backgroundColor = color
  }

  override def onAnimate(dt: Float): Unit = {}

  override def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    maskShader.get.draw(mvMatrix, pMatrix, geometry.get, (backgroundColor, backgroundColor, textColor, texture.get))
  }

  def getWidth = geometry.get.asInstanceOf[TextGeometry].width * objectScale(0)

  def getHeight = geometry.get.asInstanceOf[TextGeometry].height * objectScale(1)
}
