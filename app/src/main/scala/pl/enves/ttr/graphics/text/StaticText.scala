package pl.enves.ttr.graphics.text

import android.graphics._
import android.opengl.GLES20
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.shaders.TextureShaderData
import pl.enves.ttr.graphics.models.Rectangle
import pl.enves.ttr.utils.Logging

/**
 * Display string pre-rendered to texture
 */
class StaticText(text: String, resources: Resources, maxW: Float = 1.0f, maxH: Float = 1.0f, color: Int = Color.BLACK)
  extends Logging {

  var fontHeight, textWidth = 0.0f

  val sizePx = 256

  val scale = 1.0f

  val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_4444)
  bitmap.eraseColor(0)

  // get a canvas to paint over the bitmap
  val canvas = new Canvas(bitmap)

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

  canvas.drawText(text, 0, math.ceil(math.abs(fm.top)).toFloat, textPaint)

  resources.addProcTexture(textureName, bitmap)
  resources.addProcModel(modelName, modelGeometry)

  val drawData = new TextureShaderData(resources.getTexture(textureName))
  val rectangle = resources.getGeometry(modelName)
  val textureShader = resources.getShader(resources.ShaderId.Texture)

  def textureName: String = text + "Texture"

  def textureBitmap: Bitmap = bitmap

  def modelName: String = text + "Model"

  def modelGeometry: ProcGeometry = {
    val buffers = new ProcBuffers(Some(Rectangle.coords(textWidth / sizePx, fontHeight / sizePx)),
      None,
      None,
      Some(Rectangle.texCoords(0, 1.0f - fontHeight / sizePx, textWidth / sizePx, fontHeight / sizePx)))
    return new ProcGeometryArrays(
      Rectangle.numVertex,
      GLES20.GL_TRIANGLE_STRIP,
      buffers
    )
  }

  def draw(): Unit = {
    textureShader.draw(rectangle, drawData)
  }
}
