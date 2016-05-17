package pl.enves.ttr.graphics.texture

import android.graphics._
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._

class CharactersTexture(sizePx: Int, typeface: Typeface, charset: Seq[Char]) extends Logging with TextureUtils {

  private[this] val size: Int = Math.ceil(Math.sqrt(charset.length)).toInt

  private[this] val cellPx = sizePx / size

  private[this] val textMaskPaint = new Paint()
  textMaskPaint.setAntiAlias(true)
  textMaskPaint.setTypeface(typeface)
  textMaskPaint.setColor(Color.rgb(255, 0, 0))

  private[this] val fontSize = calculateFontSize(cellPx, cellPx)

  textMaskPaint.setTextSize(fontSize)
  private[this] val fontHeight = measureFontHeight(fontSize, textMaskPaint)
  private[this] val charsWidths = Array.fill(charset.length)(0.0f)
  for (i <- charset.indices) {
    val cw = textMaskPaint.measureText(charset(i).toString)
    charsWidths(i) = cw
  }

  private[this] val fm = textMaskPaint.getFontMetrics

  //TODO: more optimal
  //TODO: respect font ascent and descent
  private[this] def calculateFontSize(maxWidth: Int, maxHeight: Int): Int = {
    var fontSize = 10

    val paint = new Paint()
    paint.setAntiAlias(true)
    paint.setTypeface(typeface)
    paint.setTextSize(fontSize)

    while (measureFontWidth(fontSize, paint) <= maxWidth && measureFontHeight(fontSize, paint) <= maxHeight) {
      fontSize += 1
      paint.setTextSize(fontSize)
    }

    return fontSize - 1
  }

  private[this] def measureFontWidth(fontSize: Int, paint: Paint): Float = {
    var width = 0.0f
    for (i <- charset.indices) {
      val cw = paint.measureText(charset(i).toString)
      if (cw > width) {
        width = cw
      }
    }
    return width
  }

  private[this] def measureFontHeight(fontSize: Int, paint: Paint): Float = {
    val fm = paint.getFontMetrics
    val height = math.ceil(math.abs(fm.bottom) + math.abs(fm.top)).toFloat
    return height
  }

  private[this] def coordinates(x: Int) = (x % size, x / size)

  //TODO: Make this faster for dynamic text render
  private[this] def index(char: Char): Int = {
    val a = charset.indexOf(char)
    return if (a != -1) a else 0
  }

  def getNormalizedCoordinates(char: Char): (Float, Float) = {
    val (x, y) = coordinates(index(char))
    val nSize = 1.0f / size
    return (x * nSize, y * nSize)
  }

  def getNormalizedWidth(char: Char): Float = charsWidths(index(char)) / sizePx

  def getNormalizedFontHeight = fontHeight / sizePx

  def getTexture: Int = createTexture(createBitmap())

  private def createBitmap(): Bitmap = {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_4444)
    bitmap.eraseColor(0)

    // get a canvas to paint over the bitmap
    val canvas = new Canvas(bitmap)

    for (i <- charset.indices) {
      val (x, y) = coordinates(i)

      //OpenGL textures are upside-down
      val ry = size - 1 - y
      canvas.drawText(charset(i).toString, x * cellPx, ry * cellPx + Math.abs(fm.top), textMaskPaint)
    }

    return bitmap
  }
}
