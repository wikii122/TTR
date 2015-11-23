package pl.enves.ttr.graphics

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.app.AlertDialog
import android.content.Context
import android.opengl.GLSurfaceView.Renderer
import android.opengl.{GLES20, Matrix}
import android.view.MotionEvent
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.ColorTypes.Color3
import pl.enves.ttr.graphics.board.GameBoard
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.themes._

import scala.util.{Failure, Success, Try}

/**
 * Manages the process of drawing the frame.
 */
class GameRenderer(context: Context, game: Game) extends Renderer with Logging {
  log("Creating")

  private[this] val resources = new Resources(context, game)
  private[this] val board = new GameBoard(game, resources)
  private[this] var viewportWidth: Int = 1
  private[this] var viewportHeight: Int = 1
  private[this] var lastFrame: Long = 0
  private var framesLastSecond = 0
  private var themeNeedsUpdate = false

  val mvMatrix = new MatrixStack(8)
  val pMatrix = new MatrixStack()

  def setCamera(mvMatrix: MatrixStack): Unit = {
    //In case of inconsistent use of push and pop
    mvMatrix.clear()

    //We don't use camera transformations
  }

  def setTheme(theme: Theme): Unit = {
    resources.setTheme(theme)
    themeNeedsUpdate = true
  }

  override def onDrawFrame(gl: GL10) {
    if (themeNeedsUpdate) {
      updateTheme()
      themeNeedsUpdate = false
    }
    val now = System.currentTimeMillis()

    if (lastFrame != 0) {
      setCamera(mvMatrix)
      board.animate((now - lastFrame) / 1000.0f)

      this.synchronized {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT)
        board.draw(mvMatrix, pMatrix)
      }

      framesLastSecond += 1

      if (now / 1000 > lastFrame / 1000) {
        log("FPS: " + framesLastSecond)
        framesLastSecond = 0
      }
    }
    lastFrame = now
  }

  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    viewportWidth = width
    viewportHeight = height

    Matrix.setIdentityM(pMatrix.get(), 0)
    if (height > width) {
      val ratio = height.toFloat / width.toFloat
      Matrix.orthoM(pMatrix.get(), 0, -1.0f, 1.0f, -ratio, ratio, -1.0f, 1.0f)
    } else {
      val ratio = width.toFloat / height.toFloat
      Matrix.orthoM(pMatrix.get(), 0, -ratio, ratio, -1.0f, 1.0f, -1.0f, 1.0f)
    }
  }

  override def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GLES20.glClearDepthf(1.0f)
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    GLES20.glDepthFunc(GLES20.GL_LEQUAL)
    GLES20.glDepthMask(true)
    GLES20.glEnable(GLES20.GL_CULL_FACE)
    GLES20.glCullFace(GLES20.GL_BACK)
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

    resources.createOpenGLResources()
    board.updateResources()
    themeNeedsUpdate = true
  }

  private def updateTheme(): Unit = {
    val backgroundColor: Color3 = resources.getTheme.background
    GLES20.glClearColor(backgroundColor._1, backgroundColor._2, backgroundColor._3, 1.0f)
    board.updateTheme()
  }

  def onTouchEvent(e: MotionEvent): Boolean = {
    if (e.getAction == MotionEvent.ACTION_DOWN) {
      val clickX = e.getX
      val clickY = viewportHeight - e.getY
      val viewport = Array(0, 0, viewportWidth, viewportHeight)
      val tempMVMatrix = new MatrixStack(8)
      val tempPMatrix = new MatrixStack()
      pMatrix.get().copyToArray(tempPMatrix.get())
      setCamera(tempMVMatrix)
      try {
        board.click(clickX, clickY, viewport, tempMVMatrix, tempPMatrix)
      } catch {
        //TODO: remind user that game has ended
        case e: GameFinished =>
        case e: GameWon =>
        case e: GameDrawn =>
      }
    }

    return true
  }
}

object GameRenderer {
  def apply(context: Context with GameManager) = new GameRenderer(context, context.game)

  def apply(context: Context, game: Game) = new GameRenderer(context, game)
}
