package pl.enves.ttr.graphics.text

import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.{Geometry, GeometryId, TextGeometry}
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.texture.TextureId

class StaticText(geometryId: GeometryId.Value, textureId: TextureId.Value)
  extends Logging with SceneObject {

  private[this] var textColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  private[this] var texture: Option[Int] = None
  private[this] var geometry: Option[Geometry] = None
  private[this] var maskShader: Option[MaskShader] = None

  override def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    texture = Some(resources.getTexture(textureId))
    geometry = Some(resources.getGeometry(geometryId))
    maskShader = Some(resources.getMaskShader)
  }

  def setTextColor(color: ColorArray): Unit = {
    textColor = color
  }

  override def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    maskShader.get.draw(mvMatrix, pMatrix, geometry.get, textColor, texture.get)
  }

  def getWidth: Float = geometry.get.asInstanceOf[TextGeometry].getWidth

  def getHeight: Float = geometry.get.asInstanceOf[TextGeometry].getHeight
}
