package pl.enves.ttr.graphics.text

import android.graphics._
import android.opengl.GLES20
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.shaders.{TextureShader, TextureShaderData}
import pl.enves.ttr.graphics.models.Rectangle

/**
 * Display string pre-rendered to texture
 */
class StaticText(text: String, resources: Resources, maxW: Float = 1.0f, maxH: Float = 1.0f, color: Int = Color.BLACK)
  extends Logging with GeometryProvider with BitmapProvider with SceneObject {

  resources.addBitmapProvider(this)
  resources.addGeometryProvider(this)

  override def getGeometry: Map[String, GeometryData] = Map((modelName, modelGeometry))

  override def getBitmapsNames: List[String] = List(textureName)
  override def getBitmap(name: String): Bitmap = {
    if(name == textureName) {
      return createBitmap()
    }else{
      throw new NoSuchFieldException
    }
  }

  var fontHeight, textWidth = 0.0f

  val sizePx = 256

  val scale = 1.0f

  val textPaint = new Paint()
  textPaint.setAntiAlias(true)
  textPaint.setTypeface(Typeface.SANS_SERIF)
  textPaint.setColor(color)

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

    canvas.drawText(text, 0, math.ceil(math.abs(fm.top)).toFloat, textPaint)

    return bitmap
  }

  var drawData: Option[TextureShaderData] = None
  var rectangle: Option[Geometry] = None
  var textureShader: Option[TextureShader] = None

  override def onUpdateResources(): Unit = {
    drawData = Some(new TextureShaderData(resources.getTexture(textureName)))
    rectangle = Some(resources.getGeometry(modelName))
    textureShader = Some(resources.getShader(ShaderId.Texture).asInstanceOf[TextureShader])
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

  override def onAnimate(dt: Float): Unit = ???

  override def onClick(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean = ???

  override def onDraw(): Unit = {
    textureShader.get.draw(rectangle.get, drawData.get)
  }
}
