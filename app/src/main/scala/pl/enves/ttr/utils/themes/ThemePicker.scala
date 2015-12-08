package pl.enves.ttr.utils.themes

import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.{Animator, ValueAnimator}
import android.content.Context
import android.graphics.{Canvas, Paint}
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.{GestureDetector, MotionEvent, View}
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorAndroid
import pl.enves.ttr.utils.styled.Styled

class ThemePicker(context: Context, attrs: AttributeSet) extends View(context, attrs) with Logging with ColorManip {
  private var rCenter: Float = 0
  private var rSide: Float = 0
  private var centerY: Float = 0
  private var centerX: Float = 0

  private var leftEdge: Float = 0
  private var rightEdge: Float = 0
  private var padding: Float = 0

  private val minSpace = 10

  private var currentPicked: Int = 0
  private var listPosition: Float = 0.0f

  private var listScaleX: Float = 1.0f

  private var changed: Boolean = false

  private object State extends Enumeration {
    type State = Value
    val Normal, Dragged, Animation = Value
  }

  private var state = State.Normal

  private var themes: Array[Theme] = Array[Theme]()

  private var paints: Array[Paints] = Array[Paints]()

  private val detector = new GestureDetector(getContext, new GestureListener())

  private var changeListener: Option[Styled] = None

  private var animator: Option[ValueAnimator] = None

  /**
   * Positions in pixels only
   */
  private object Circle {

    private def sampleRadius(sideX: Float): Float = {
      val a = (rSide - rCenter) / (centerX - leftEdge)
      val b = rCenter
      val x = Math.abs(centerX - sideX)

      return a * x + b
    }

    def draw(canvas: Canvas, x: Float, y: Float, paints: Paints): Unit = {
      val radius = sampleRadius(x)
      canvas.drawCircle(x, y, radius, paints.backgroundPaint)
      val a = (radius / 4) * Math.sqrt(2).toFloat
      canvas.drawCircle(x - a, y - a, radius / 2, paints.color1Paint)
      canvas.drawCircle(x + a, y + a, radius / 2, paints.color2Paint)
    }

    def checkClick(clickX: Float, clickY: Float, x: Float, y: Float): Boolean = {
      val r = rCenter
      val dx = clickX - x
      val dy = clickY - y
      return dx * dx + dy * dy < r * r
    }
  }

  private class Paints(theme: Theme) {
    val color1Paint = makePaint(theme.color1)
    val color2Paint = makePaint(theme.color2)
    val backgroundPaint = makePaint(theme.background)

    private def makePaint(color: ColorAndroid): Paint = {
      val p = new Paint(0)
      p.setStyle(Paint.Style.FILL_AND_STROKE)
      p.setAntiAlias(true)
      p.setColor(color)
      return p
    }

    def getTheme: Theme = theme
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {
    override def onDown(e: MotionEvent): Boolean = {
      return true
    }

    override def onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      state = State.Dragged
      movePosition(distanceX / listScaleX)
      currentFromPosition()
      return true
    }

    override def onSingleTapUp(e: MotionEvent): Boolean = {
      return checkClick(e.getX, e.getY)
    }
  }

  private def foreachVisible(f: (Int, Float) => Unit): Unit = {
    var index = 0
    var positionX = 0.0f
    val skew = listPosition - Math.floor(listPosition).toFloat

    //right side + center
    positionX = centerX - skew * listScaleX
    index = Math.floor(listPosition).toInt
    while (positionX <= rightEdge + padding + rSide) {
      f(index, positionX)
      index += 1
      if (index >= paints.length) {
        index = 0
      }
      positionX += 1 * listScaleX
    }

    //left side
    positionX = centerX - (skew + 1) * listScaleX
    index = Math.floor(listPosition).toInt - 1
    if (index < 0) {
      index = paints.length - 1
    }
    while (positionX >= leftEdge - padding - rSide) {
      f(index, positionX)
      index -= 1
      if (index < 0) {
        index = paints.length - 1
      }
      positionX -= 1 * listScaleX
    }
  }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (!isInEditMode) {
      def f(index: Int, positionX: Float): Unit = {
        Circle.draw(canvas, positionX, centerY, paints(index))
      }

      foreachVisible(f)

      if (state == State.Animation || state == State.Dragged) {
        changeColors()
      }
    }
  }

  private def setupAnimation(): Unit = {
    state = State.Animation
    val diff = getRingDistance(currentPicked, listPosition, paints.length.toFloat)

    animator = Some(ValueAnimator.ofFloat(listPosition, listPosition + diff))
    animator.get.setDuration((500 * Math.abs(diff)).toInt)
    animator.get.setInterpolator(new AccelerateDecelerateInterpolator())
    animator.get.addUpdateListener(new AnimatorUpdateListener {
      override def onAnimationUpdate(animation: ValueAnimator): Unit = {
        setPosition(animation.getAnimatedValue.asInstanceOf[Float])
        invalidate()
      }
    })
    animator.get.addListener(new AnimatorListener {
      override def onAnimationEnd(animation: Animator): Unit = {
        state = State.Normal
        animator = None
      }

      override def onAnimationRepeat(animation: Animator): Unit = {}

      override def onAnimationStart(animation: Animator): Unit = {}

      override def onAnimationCancel(animation: Animator): Unit = {}
    })
    animator.get.start()
  }

  private def cancelAnimation(): Unit = {
    state = State.Normal
    if (animator.isDefined) {
      animator.get.cancel()
    }
    animator = None
  }

  private def checkClick(clickX: Float, clickY: Float): Boolean = {
    def f(index: Int, positionX: Float): Unit = {
      if (Circle.checkClick(clickX, clickY, positionX, centerY)) {
        log("clicked " + index.toString)
        if (index != currentPicked) {
          currentPicked = index
          cancelAnimation()
          setupAnimation()
        }
      }
    }

    foreachVisible(f)
    return true
  }

  private def getRingDistance(a: Float, b: Float, modulo: Float): Float = {
    if (a < b) {
      val diff1 = b - a
      val diff2 = a + (modulo - b)
      if (diff1 < diff2) -diff1 else diff2
    } else {
      val diff1 = a - b
      val diff2 = b + (modulo - a)
      if (diff1 < diff2) diff1 else -diff2
    }
  }

  private def changeColors(): Unit = {
    if (changeListener.isDefined) {
      val first = Math.floor(listPosition).toInt
      var second = Math.ceil(listPosition).toInt
      if (second >= paints.length) {
        second = 0
      }
      val firstTheme = paints(first).getTheme
      val secondTheme = paints(second).getTheme
      val diff = listPosition - first

      val b = colorLerp(firstTheme.background, secondTheme.background, diff)
      val c1 = colorLerp(firstTheme.color1, secondTheme.color1, diff)
      val c2 = colorLerp(firstTheme.color2, secondTheme.color2, diff)
      val w = colorLerp(firstTheme.winner, secondTheme.winner, diff)
      val theme = Theme(b, c1, c2, w)
      changeListener.get.setColorTheme(theme)
    }
  }

  override def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int): Unit = {
    super.onSizeChanged(w, h, oldw, oldh)

    rCenter = h / 2
    rSide = rCenter / 2

    padding = rSide * 3

    leftEdge = padding
    rightEdge = w - padding

    centerY = h / 2
    centerX = w / 2

    val space = rightEdge - leftEdge
    var sideSamples = Math.floor((space - 2 * rCenter) / (2 * (2 * rCenter + minSpace))).toInt
    if (sideSamples < 1) {
      sideSamples = 1
    }

    listScaleX = space / (2 * sideSamples)
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    changed = true
    val result = detector.onTouchEvent(event)
    if (!result) {
      if (event.getAction == MotionEvent.ACTION_UP) {
        cancelAnimation()
        setupAnimation()
      }
    }
    invalidate()
    return result
  }

  def setCurrent(n: Int) = {
    currentPicked = n
    listPosition = n.toFloat
  }

  def movePosition(dx: Float) = {
    setPosition(listPosition + dx)
  }

  def setPosition(x: Float) = {
    val s = paints.length.toFloat
    listPosition = x

    if (listPosition < 0.0f) {
      listPosition = listPosition % s + s
    }

    if (listPosition >= s) {
      listPosition = listPosition % s
    }
  }

  def currentFromPosition(): Unit = {
    currentPicked = Math.round(listPosition)
    if (currentPicked >= paints.length) {
      currentPicked = 0
    }
  }

  def setChangeListener(listener: Styled) = changeListener = Some(listener)

  def setThemes(t: Array[Theme]): Unit = {
    themes = t
    paints = themes.map(theme => new Paints(theme))
  }

  def setInitialTheme(theme: Theme): Unit = {
    for (i <- paints.indices) {
      if (theme == paints(i).getTheme) {
        setCurrent(i)
      }
    }
  }

  def getTheme: Theme = paints(currentPicked).getTheme

  def isChanged: Boolean = changed
}