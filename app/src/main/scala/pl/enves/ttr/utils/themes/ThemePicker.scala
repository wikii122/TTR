package pl.enves.ttr.utils.themes

import android.content.Context
import android.content.res.TypedArray
import android.graphics.{Canvas, Color, Paint, RectF}
import android.util.AttributeSet
import android.view.{GestureDetector, MotionEvent, View}
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorAndroid
import pl.enves.ttr.R

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


//TODO: Use Android animations
class ThemePicker(context: Context, attrs: AttributeSet) extends View(context, attrs) with Logging with ColorManip {
  private var rCenter: Float = 0
  private var rSide: Float = 0
  private var centerY: Float = 0
  private var centerX: Float = 0
  private var sideXMax: Float = 0

  private var sideSamples = 1

  private def spacing = sideXMax / sideSamples

  private object State extends Enumeration {
    type State = Value
    val Normal, Dragged, Animation = Value
  }

  private var state = State.Normal

  private var drift = 0.0f

  private val themes: mutable.ArrayBuffer[Theme] = readDefaultThemes
  //TODO: append user-made themes

  private val paints: ArrayBuffer[(Paint, Paint, Paint)] = themes.map(
    theme => (makePaint(theme.outer1),
      makePaint(theme.outer2),
      makePaint(theme.background))
  )

  private val backgrounds: ArrayBuffer[ColorAndroid] = themes.map(theme => theme.background)

  private var current: Int = 0

  private val detector = new GestureDetector(getContext, new gestureListener())

  private var changed = false

  type ColorChanger = (ColorAndroid, ColorAndroid, ColorAndroid) => Unit

  private var colorChanger: Option[ColorChanger] = None

  private class gestureListener extends GestureDetector.SimpleOnGestureListener {
    var startX: Float = 0.0f

    override def onDown(e: MotionEvent): Boolean = {
      startX = e.getX
      state = State.Dragged
      return true
    }

    override def onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      if (state == State.Dragged) {
        val diff = startX - e2.getX
        drift = diff / spacing

        if (Math.abs(drift) >= 0.5f) {
          if (drift > 0) {
            current = right()
            drift = -0.5f
          } else {
            current = left()
            drift = 0.5f
          }
          state = State.Animation
          changed = true
        }
      }
      return true
    }
  }

  private def makePaint(color: ColorAndroid): Paint = {
    val p = new Paint(0)
    p.setStyle(Paint.Style.FILL_AND_STROKE)
    p.setAntiAlias(true)
    p.setColor(color)
    return p
  }

  private def sampleRadius(sideX: Float): Float = {
    val a = (rSide - rCenter) / sideXMax
    val b = rCenter

    return a * sideX + b
  }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (!isInEditMode) {
      val driftedCenter = centerX - spacing * drift

      drawSample(canvas, driftedCenter, centerY, current, Math.abs(drift))

      for (i <- 1 to sideSamples) {
        val sideX = i * spacing
        val bLeft = if(i == 1 && drift < 0) 1.0f - Math.abs(drift) else 1.0f
        val bRight = if(i == 1 && drift > 0) 1.0f - Math.abs(drift) else 1.0f
        drawSample(canvas, driftedCenter - sideX, centerY, left(i), bLeft)
        drawSample(canvas, driftedCenter + sideX, centerY, right(i), bRight)
      }

      if (state == State.Animation) {
        drift *= 0.95f
        if (Math.abs(drift) < 0.001f) {
          drift = 0.0f
          state = State.Normal
        }
        invalidate()
      }
      if (state == State.Animation || state == State.Dragged) {
        changeColors()
      }
    }
  }

  private def changeColors(): Unit = {
    if (colorChanger.isDefined) {
      val other = if (drift > 0) right() else left()
      val b = colorLerp(backgrounds(current), backgrounds(other), Math.abs(drift))
      val c1 = colorLerp(paints(current)._1.getColor, paints(other)._1.getColor, Math.abs(drift))
      val c2 = colorLerp(paints(current)._2.getColor, paints(other)._2.getColor, Math.abs(drift))
      colorChanger get(b, c1, c2)
    }
  }

  private def drawSample(canvas: Canvas, x: Float, y: Float, theme: Int, bgSize: Float): Unit = {
    val radius = sampleRadius(Math.abs(centerX - x))
    canvas.drawCircle(x, y, radius, paints(theme)._3)
    val size = (1.0f - 0.4f * bgSize) * radius
    val rect: RectF = new RectF(x - size, y - size, x + size, y + size)
    canvas.drawArc(rect, 0, 90, true, paints(theme)._2) //Second
    canvas.drawArc(rect, 90, 180, true, paints(theme)._1) //First
    canvas.drawArc(rect, 180, 270, true, paints(theme)._2) //Third
    canvas.drawArc(rect, 0, -90, true, paints(theme)._1) //Fourth
  }

  private def readDefaultThemes: mutable.ArrayBuffer[Theme] = {
    val themes: mutable.ArrayBuffer[Theme] = new mutable.ArrayBuffer[Theme]()
    val resources = context.getResources
    val themeArrays: TypedArray = resources.obtainTypedArray(R.array.themes)
    for (i <- 0 until themeArrays.length) {
      themes.append(Theme(resources, themeArrays.getResourceId(i, -1)))
    }
    themeArrays.recycle()
    return themes
  }

  override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int): Unit = {
    super.onSizeChanged(w, h, oldw, oldh)

    val ww = w - getPaddingLeft - getPaddingRight
    val hh = h - getPaddingTop - getPaddingBottom

    rCenter = hh / 2
    rSide = rCenter / 2.0f

    centerY = getPaddingTop + hh / 2
    centerX = getPaddingLeft + ww / 2
    sideXMax = centerX - rSide

    sideSamples = 1
    var done = false
    while (!done) {
      sideSamples += 1
      if (rCenter + sampleRadius(spacing) > spacing) {
        sideSamples -= 1
        done = true
      }
    }
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    val result = detector.onTouchEvent(event)
    if (!result) {
      if (event.getAction == MotionEvent.ACTION_UP) {
        state = State.Animation
      }
    }
    invalidate()
    return result
  }

  def left(n: Int = 1): Int = {
    val s = themes.length
    return (current - (n % s) + s) % s
  }

  def right(n: Int = 1): Int = {
    return (current + n) % themes.length
  }

  def getCurrentJSON: String = themes(current).toJsonObject.toString

  def getDefaultJSON: String = themes.head.toJsonObject.toString

  def setCurrentFromJSON(themeJSON: String): Unit = {
    val theme = Theme(themeJSON)
    var i = themes.indexOf(theme)
    if (i == -1) {
      i = 0
    }
    current = i
    changeColors()
  }

  def hasChanged: Boolean = changed

  def setColorChanger(cc: ColorChanger): Unit = {
    colorChanger = Some(cc)
  }
}