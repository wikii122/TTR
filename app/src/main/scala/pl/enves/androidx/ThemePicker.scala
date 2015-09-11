package pl.enves.androidx

import android.content.Context
import android.graphics.{Canvas, Paint}
import android.util.AttributeSet
import android.view.{GestureDetector, MotionEvent, View}
import pl.enves.ttr.graphics.themes.{Theme, ThemeId}

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

  private val paints: Map[ThemeId.Value, Paint] = Theme.all(context.getResources).mapValues(theme => makePaint(theme.background))

  private var current: ThemeId.Value = ThemeId.Blue

  private val detector = new GestureDetector(getContext, new gestureListener())

  private var changed = false

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
    p.setStyle(Paint.Style.FILL)
    p.setAntiAlias(true)
    p.setColor(color)
    return p
  }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val a = rCenter - rSide
    canvas.drawCircle(centerX - sideX * drift, centerY, -a * Math.abs(drift) + rCenter, paints(current))
    canvas.drawCircle(centerX - sideX - sideX * drift, centerY, -a * drift + rSide, paints(left()))
    canvas.drawCircle(centerX + sideX - sideX * drift, centerY, a * drift + rSide, paints(right()))

    if (state == State.Animation) {
      drift *= 0.95f
      if (Math.abs(drift) < 0.001f) {
        drift = 0.0f
        state = State.Normal
      }
      invalidate()
    }
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

  def getTheme(): String = current.toString

  def setTheme(theme: String): Unit = {
    current = ThemeId.values.find(_.toString == theme).getOrElse(ThemeId(0))
  }

  def getDefaultTheme: String = ThemeId(0).toString

  def hasChanged: Boolean = changed
}