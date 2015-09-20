package pl.enves.ttr.graphics.text

import android.graphics._
import android.opengl.GLES20
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorTypes
import ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.models.Rectangle
import pl.enves.ttr.graphics.shaders.MaskShader

/**
 * Display string pre-rendered to texture
 */
class StaticText(text: String, resources: Resources, maxW: Float = 1.0f, maxH: Float = 1.0f)
  extends Logging with GeometryProvider with TextureProvider with SceneObject {

  resources.addBitmapProvider(this)
  resources.addGeometryProvider(this)

  override def getGeometry: Map[String, GeometryData] = Map((modelName, modelGeometry))

  override def getTextures: Map[String, Int] = {
    Map((textureName, createTexture(createBitmap())))
  }

  var fontHeight, textWidth = 0.0f

  val sizePx = 256

  val scale = 1.0f

  val backgroundPaint = new Paint()
  backgroundPaint.setColor(Color.rgb(0, 255, 0))

  val textPaint = new Paint()
  textPaint.setAntiAlias(true)
  textPaint.setTypeface(resources.comfortaa)
  textPaint.setColor(Color.rgb(0, 0, 255))

  var textColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)
  var backgroundColor: ColorArray = Array(0.0f, 0.0f, 0.0f, 0.0f)

  //Find optimal font size
  //TODO: more optimal
  //TODO: respect font ascent and descent
  var fontSize = 10
  var fm = textPaint.getFontMetrics
  while (fontHeight < sizePx * maxH && textWidth < sizePx * maxW && fontSize < 128) {
    textPaint.setTextSize(fontSize)
    fm = textPaint.getFontMetrics
    fontHeight = math.ceil(math.abs(fm.bottom) + math.abs(fm.top)).toFloat
    textWidth = textPaint.measureText(text)
    fontSize += 1
  }

  log("Font size: " + fontSize)
  log("Font height: " + fontHeight)
  log("Font top: " + fm.top)
  log("Font bottom: " + fm.bottom)
  log("Text width: " + textWidth)

  def createBitmap(): Bitmap = {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_4444)
    bitmap.eraseColor(0)

    // get a canvas to paint over the bitmap
    val canvas = new Canvas(bitmap)

    canvas.drawRect(0, 0, sizePx, sizePx, backgroundPaint)
    canvas.drawText(text, 0, math.ceil(math.abs(fm.top)).toFloat, textPaint)

    return bitmap
  }

  var texture: Option[Int] = None
  var rectangle: Option[Geometry] = None
  var maskShader: Option[MaskShader] = None

  override def onUpdateResources(): Unit = {
    texture = Some(resources.getTexture(textureName))
    rectangle = Some(resources.getGeometry(modelName))
    maskShader = Some(resources.getShader(ShaderId.Mask).asInstanceOf[MaskShader])
  }

  override protected def onUpdateTheme(): Unit = {}

  def setTextColor(color: ColorArray): Unit = {
    textColor = color
  }

  def setTextBackground(color: ColorArray): Unit = {
    backgroundColor = color
  }

  def textureName: String = text + "Texture"

  def modelName: String = text + "Model"

  def modelGeometry: GeometryData = {
    val buffers = new BuffersData(Some(Rectangle.coords(textWidth / sizePx, fontHeight / sizePx)),
      None,
      None,
      Some(Rectangle.texCoords(0, 1.0f - fontHeight / sizePx, textWidth / sizePx, fontHeight / sizePx)))
    return new ArraysGeometryData(
      Rectangle.numVertex,
      GLES20.GL_TRIANGLE_STRIP,
      buffers
    )
  }

  override def onAnimate(dt: Float): Unit = {}

  override def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    maskShader.get.draw(mvMatrix, pMatrix, rectangle.get, (backgroundColor, backgroundColor, textColor, texture.get))
  }
}
