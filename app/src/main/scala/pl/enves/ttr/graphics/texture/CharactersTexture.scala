package pl.enves.ttr.graphics.texture

import android.graphics._
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._

class CharactersTexture(sizePx: Int, typeface: Typeface, charset: Array[Char]) extends Logging with TextureProvider {

  private val size: Int = Math.ceil(Math.sqrt(charset.length)).toInt

  private val cellPx = sizePx / size

  private val backgroundMaskPaint = new Paint()
  backgroundMaskPaint.setColor(Color.rgb(0, 255, 0))

  private val textMaskPaint = new Paint()
  textMaskPaint.setAntiAlias(true)
  textMaskPaint.setTypeface(typeface)
  textMaskPaint.setColor(Color.rgb(0, 0, 255))

  //Find optimal font size
  //TODO: more optimal
  //TODO: respect font ascent and descent
  private var fontSize = 10
  while (measureFont(fontSize + 1)) {
    fontSize += 1
  }

  textMaskPaint.setTextSize(fontSize)
  private val fontHeight = measureFontHeight(fontSize, textMaskPaint)
  private val charWidthMax = measureFontWidth(fontSize, textMaskPaint)
  private val charsWidths = Array.fill(charset.length)(0.0f)
  for (i <- charset.indices) {
    val cw = textMaskPaint.measureText(charset(i).toString)
    charsWidths(i) = cw
  }
  private val fm = textMaskPaint.getFontMetrics

  log("Font size: " + fontSize)
  log("Font height: " + fontHeight)
  log("Font top: " + fm.top)
  log("Font bottom: " + fm.bottom)
  log("Text width: " + charWidthMax)

  private def measureFont(fontSize: Int): Boolean = {
    val paint = new Paint()
    paint.setAntiAlias(true)
    paint.setTypeface(typeface)
    paint.setTextSize(fontSize)
    return measureFontWidth(fontSize, paint) <= cellPx && measureFontHeight(fontSize, paint) <= cellPx
  }

  private def measureFontWidth(fontSize: Int, paint: Paint): Float = {
    var width = 0.0f
    for (i <- charset.indices) {
      val cw = paint.measureText(charset(i).toString)
      if (cw > width) {
        width = cw
      }
    }
    return width
  }

  private def measureFontHeight(fontSize: Int, paint: Paint): Float = {
    val fm = paint.getFontMetrics
    val height = math.ceil(math.abs(fm.bottom) + math.abs(fm.top)).toFloat
    return height
  }

  def coordinates(x: Int) = (x % size, x / size)

  //TODO: Make this faster for dynamic text render
  def index(char: Char): Int = {
    val a = charset.indexOf(char)
    return if (a != -1) a else 0
  }

  def getNormalizedCoordinates(char: Char): (Float, Float) = {
    val (x, y) = coordinates(index(char))
    val nsize = 1.0f / size
    return (x * nsize, y * nsize)
  }

  def getNormalizedWidth(char: Char): Float = charsWidths(index(char)) / sizePx

  def getNormalizedFontHeight = fontHeight / sizePx

  override def getTexture: Int = createTexture(createBitmap())

  private def createBitmap(): Bitmap = {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_4444)
    bitmap.eraseColor(0)

    // get a canvas to paint over the bitmap
    val canvas = new Canvas(bitmap)

    canvas.drawRect(0, 0, sizePx, sizePx, backgroundMaskPaint)

    for (i <- charset.indices) {
      val (x, y) = coordinates(i)

      //OpenGL textures are upside-down
      val ry = size - 1 - y
      canvas.drawText(charset(i).toString, x * cellPx, ry * cellPx + Math.abs(fm.top), textMaskPaint)
    }

    return bitmap
  }
}