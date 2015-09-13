package pl.enves.androidx

import android.content.Context
import android.graphics.{Canvas, Color, Paint}
import android.util.AttributeSet
import android.view.{GestureDetector, MotionEvent, View}
import pl.enves.ttr.graphics.themes.{Theme, ThemeId}


//TODO: Use Android animations
//TODO: Use callback provided by activity instead of getRootView to change background
class ThemePicker(context: Context, attrs: AttributeSet) extends View(context, attrs) {
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

  private val paints: Map[ThemeId.Value, (Paint, Paint)] = Theme.all(context.getResources).mapValues(
    theme => (makePaint(theme.outer1), makePaint(theme.outer2))
  )

  private val backgrounds: Map[ThemeId.Value, Int] = Theme.all(context.getResources).mapValues(theme => theme.background)

  private var current: ThemeId.Value = ThemeId.Blue

  private val detector = new GestureDetector(getContext, new gestureListener())

  private var changed = false

  private var oldBackgroundColor = Color.WHITE

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
            setCurrent(right())
            drift = -0.5f
          } else {
            setCurrent(left())
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
          oldBackgroundColor = backgrounds(current)
        }
        getRootView.setBackgroundColor(lerpColor(oldBackgroundColor, backgrounds(current), 1 - 2 * Math.abs(drift)))
        invalidate()
      }
    }
  }

  private def drawSample(canvas: Canvas, x: Float, y: Float, size: Float, themeId: ThemeId.Value): Unit = {
    //canvas.drawCircle(x, y, size, paints(themeId)._1)
    canvas.drawRect(x - size, y, x, y + size, paints(themeId)._1) //First
    canvas.drawRect(x, y, x + size, y + size, paints(themeId)._2) //Second
    canvas.drawRect(x - size, y - size, x, y, paints(themeId)._2) //Third
    canvas.drawRect(x, y - size, x + size, y, paints(themeId)._1) //Fourth
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

  override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int): Unit = {
    // Account for padding
    val xpad = getPaddingLeft + getPaddingRight
    val ypad = getPaddingTop + getPaddingBottom

    val ww = w - xpad
    val hh = h - ypad

    rCenter = hh / 2
    rSide = rCenter / 1.5f

    centerY = hh / 2
    centerX = ww / 2
    sideX = (ww / 2 - rSide) * 0.9f
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    val result = detector.onTouchEvent(event)
    if (!result) {
      if (event.getAction() == MotionEvent.ACTION_UP) {
        state = State.Animation
      }
    }
    invalidate()
    return result
  }

  def left(): ThemeId.Value = {
    var id = current.id - 1
    if (id < 0) {
      id = ThemeId.values.size - 1
    }
    return ThemeId(id)
  }

  def right(): ThemeId.Value = {
    var id = current.id + 1
    if (id >= ThemeId.values.size) {
      id = 0
    }
    return ThemeId(id)
  }

  def getCurrent: String = current.toString

  def setCurrent(themeId: ThemeId.Value): Unit = {
    oldBackgroundColor = backgrounds(current)
    current = themeId
  }

  def setCurrent(theme: String): Unit = {
    setCurrent(ThemeId.values.find(_.toString == theme).getOrElse(ThemeId(0)))
    oldBackgroundColor = backgrounds(current)
    getRootView.setBackgroundColor(backgrounds(current))
  }

  def getDefaultTheme: String = ThemeId(0).toString

  def hasChanged: Boolean = changed
}