package pl.enves.androidx

import android.content.Context
import android.content.res.TypedArray
import android.graphics.{RectF, Canvas, Color, Paint}
import android.util.AttributeSet
import android.view.{GestureDetector, MotionEvent, View}
import pl.enves.ttr.R
import pl.enves.ttr.graphics.themes.Theme

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


//TODO: Use Android animations
class ThemePicker(context: Context, attrs: AttributeSet) extends View(context, attrs) with Logging {
  private var rCenter: Float = 0
  private var rSide: Float = 0
  private var centerY: Float = 0
  private var centerX: Float = 0
  private var sideX: Float = 0

  private object State extends Enumeration {
    type State = Value
    val Normal, Dragged, Animation = Value
  }

  private var state = State.Normal

  private var drift = 0.0f

  private val themes: mutable.ArrayBuffer[Theme] = readDefaultThemes
  //TODO: append user-made themes

  private val paints: ArrayBuffer[(Paint, Paint)] = themes.map(
    theme => (makePaint(theme.outer1), makePaint(theme.outer2))
  )

  private val backgrounds: ArrayBuffer[Int] = themes.map(theme => theme.background)

  private var current: Int = 0

  private val detector = new GestureDetector(getContext, new gestureListener())

  private var changed = false

  type ColorChanger = (Int, Int, Int) => Unit

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
        drift = diff / sideX

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

  private def makePaint(color: Int): Paint = {
    val p = new Paint(0)
    p.setStyle(Paint.Style.FILL_AND_STROKE)
    p.setAntiAlias(true)
    p.setColor(color)
    return p
  }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (!isInEditMode) {
      val a = rCenter - rSide
      drawSample(canvas, centerX - sideX * drift, centerY, -a * Math.abs(drift) + rCenter, current)
      drawSample(canvas, centerX - sideX - sideX * drift, centerY, -a * drift + rSide, left())
      drawSample(canvas, centerX + sideX - sideX * drift, centerY, a * drift + rSide, right())

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
      val b = lerpColor(backgrounds(current), backgrounds(other), Math.abs(drift))
      val c1 = lerpColor(paints(current)._1.getColor, paints(other)._1.getColor, Math.abs(drift))
      val c2 = lerpColor(paints(current)._2.getColor, paints(other)._2.getColor, Math.abs(drift))
      colorChanger get(b, c1, c2)
    }
  }

  private def drawSample(canvas: Canvas, x: Float, y: Float, size: Float, theme: Int): Unit = {
    val rect: RectF = new RectF(x - size, y - size, x + size, y + size)
    canvas.drawArc(rect, 0, 90, true, paints(theme)._2) //Second
    canvas.drawArc(rect, 90, 180, true, paints(theme)._1) //First
    canvas.drawArc(rect, 180, 270, true, paints(theme)._2) //Third
    canvas.drawArc(rect, 0, -90, true, paints(theme)._1) //Fourth
  }

  private def lerpChannel(channel1: Int, channel2: Int, t: Float): Int = {
    return channel1 + (t * (channel2 - channel1)).toInt
  }

  private def lerpColor(color1: Int, color2: Int, t: Float): Int = {
    val a = lerpChannel(Color.alpha(color1), Color.alpha(color2), t)
    val r = lerpChannel(Color.red(color1), Color.red(color2), t)
    val g = lerpChannel(Color.green(color1), Color.green(color2), t)
    val b = lerpChannel(Color.blue(color1), Color.blue(color2), t)

    return Color.argb(a, r, g, b)
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
    rSide = rCenter / 1.5f

    centerY = getPaddingTop + hh / 2
    centerX = getPaddingLeft + ww / 2
    sideX = centerX - rSide
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

  def left(): Int = {
    var i = current - 1
    if (i < 0) {
      i = themes.length - 1
    }
    return i
  }

  def right(): Int = {
    var i = current + 1
    if (i >= themes.length) {
      i = 0
    }
    return i
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