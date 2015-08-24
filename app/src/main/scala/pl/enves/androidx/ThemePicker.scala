package pl.enves.androidx

import android.content.Context
import android.graphics.{Paint, Canvas}
import android.util.AttributeSet
import android.view.{GestureDetector, MotionEvent, View}
import pl.enves.ttr.graphics.themes.{Themes, ColorId, ThemeId}

class ThemePicker(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  private var rCenter: Float = 0
  private var rSide: Float = 0
  private var centerY: Float = 0
  private var centerX: Float = 0
  private var sideX: Float = 0

  private val paints: Map[ThemeId.Value, Paint] = Themes.byName.mapValues(theme => makePaint(theme.android(ColorId.background)))

  private var current: ThemeId.Value = ThemeId.Blue

  private val detector = new GestureDetector(getContext, new gestureListener())

  private def makePaint(color: Int): Paint = {
    val p = new Paint(0)
    p.setStyle(Paint.Style.FILL)
    p.setAntiAlias(true)
    p.setColor(color)
    return p
  }

  private class gestureListener extends GestureDetector.SimpleOnGestureListener {
    var startX: Float = 0.0f
    var distance: Float = 0.0f
    override def onDown(e: MotionEvent): Boolean = {
      startX = e.getX
      distance = sideX/2
      return true
    }
    override def onScroll (e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      val diff = startX - e2.getX
      if(Math.abs(diff) > distance) {
        if(diff > 0) {
          current = right()
        }else{
          current = left()
        }
        invalidate()
        startX = e2.getX
        return true
      }
      return false
    }
  }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    canvas.drawCircle(centerX, centerY, rCenter, paints(current))
    canvas.drawCircle(centerX-sideX, centerY, rSide, paints(left()))
    canvas.drawCircle(centerX+sideX, centerY, rSide, paints(right()))
  }

  override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int): Unit = {
    // Account for padding
    val xpad = getPaddingLeft + getPaddingRight
    val ypad = getPaddingTop + getPaddingBottom

    val ww = w - xpad
    val hh = h - ypad

    rCenter = hh/2
    rSide = rCenter/1.5f

    centerY = hh/2
    centerX = ww/2
    sideX = ww/2-rSide
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    val result = detector.onTouchEvent(event)
    if (!result) {
      //if (event.getAction() == MotionEvent.ACTION_UP) {
      //  result = true;
      //}
    }
    return result
  }

  def left(): ThemeId.Value = {
    var id = current.id - 1
    if(id < 0) {
      id = ThemeId.values.size - 1
    }
    return ThemeId(id)
  }

  def right(): ThemeId.Value = {
    var id = current.id + 1
    if(id >= ThemeId.values.size) {
      id = 0
    }
    return ThemeId(id)
  }

  def getTheme(): String = current.toString

  def setTheme(theme: String): Unit = {
    current = ThemeId withName(theme)
  }

  def getDefaultTheme: String = ThemeId(0).toString
}