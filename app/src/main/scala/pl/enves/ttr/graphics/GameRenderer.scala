package pl.enves.ttr.graphics

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.opengl.GLSurfaceView.Renderer
import android.opengl.{GLES20, Matrix}
import android.view.MotionEvent
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.ColorTypes.Color3
import pl.enves.ttr.graphics.board.GameBoard
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.math.Algebra
import pl.enves.ttr.utils.themes._

/**
 * Manages the process of drawing the frame.
 */
class GameRenderer(context: Context with GameManager, onEnd: () => Unit) extends Renderer with Logging with Algebra {
  log("Creating")

  private[this] val resources = new Resources(context, context.game)
  private[this] val board = new GameBoard(makeMove)
  private[this] var viewportWidth: Int = 1
  private[this] var viewportHeight: Int = 1
  private[this] var lastFrame: Long = 0
  private[this] var framesLastSecond = 0
  private[this] var themeNeedsUpdate = false
  private[this] var _theme = Theme(0, 0, 0, 0)

  private[this] val mvMatrix = new MatrixStack(8)
  private[this] val pMatrix = new MatrixStack()


  def makeMove(move: Move): Unit = this.synchronized {
    context.game.make(move)
  }

  def setCamera(mvMatrix: MatrixStack): Unit = {
    //In case of inconsistent use of push and pop
    mvMatrix.clear()

    //We don't use camera transformations
  }

  // this method may be called anytime
  def setTheme(theme: Theme): Unit = {
    _theme = theme
    themeNeedsUpdate = true
  }

  // this method may be called only when OpenGL context is valid
  private def updateTheme(theme: Theme): Unit = {
    val backgroundColor: Color3 = theme.background
    GLES20.glClearColor(backgroundColor._1, backgroundColor._2, backgroundColor._3, 1.0f)
    board.updateTheme(theme)
  }

  override def onDrawFrame(gl: GL10) {
    if (themeNeedsUpdate) {
      updateTheme(_theme)
      themeNeedsUpdate = false
    }
    val now = System.currentTimeMillis()

    if (lastFrame != 0) {

      this.synchronized {
        board.syncState(context.game)
      }

      setCamera(mvMatrix)
      board.animate((now - lastFrame) / 1000.0f)

      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT)
      board.draw(mvMatrix, pMatrix)

      framesLastSecond += 1

      if (now / 1000 > lastFrame / 1000) {
        //log("FPS: " + framesLastSecond)
        framesLastSecond = 0
      }
    }
    lastFrame = now
  }

  /**
   * Called after the surface is created
   */
  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    viewportWidth = width
    viewportHeight = height

    Matrix.setIdentityM(pMatrix.get(), 0)
    var ratio = 1.0f
    if (height > width) {
      ratio = height.toFloat / width.toFloat
      Matrix.orthoM(pMatrix.get(), 0, -1.0f, 1.0f, -ratio, ratio, -1.0f, 1.0f)
    } else {
      ratio = width.toFloat / height.toFloat
      Matrix.orthoM(pMatrix.get(), 0, -ratio, ratio, -1.0f, 1.0f, -1.0f, 1.0f)
    }
    board.reset()
    board.updateResources(resources, ratio)
  }

  override def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GLES20.glEnable(GLES20.GL_CULL_FACE)
    GLES20.glCullFace(GLES20.GL_BACK)
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

    resources.createOpenGLResources()
    themeNeedsUpdate = true
  }

  def onTouchEvent(e: MotionEvent): Boolean = {
    if (e.getAction == MotionEvent.ACTION_DOWN) {
      if (context.game.finished || context.game.gameType == Game.REPLAY) {
        onEnd()
        return true
      }

      val clickX = e.getX
      val clickY = viewportHeight - e.getY
      val viewport = Array(0, 0, viewportWidth, viewportHeight)
      val tempMVMatrix = new MatrixStack(8)
      val tempPMatrix = new MatrixStack()
      pMatrix.get().copyToArray(tempPMatrix.get())
      setCamera(tempMVMatrix)
      try {
        val ray = unProjectMatrices(tempMVMatrix.get(), tempPMatrix.get(), clickX, clickY, viewport)
        try {
          board.click(ray, tempMVMatrix)
        } catch {
          //TODO: remind user that game has ended
          case e: GameFinished =>
          case e: GameWon =>
          case e: GameDrawn =>
        }
      } catch {
        case e: UnProjectException =>
          error(e.getMessage)
          return false
      }
    }

    return true
  }
}

object GameRenderer {
  def apply(context: Context with GameManager, onEnd: () => Unit) = new GameRenderer(context, onEnd)
}
