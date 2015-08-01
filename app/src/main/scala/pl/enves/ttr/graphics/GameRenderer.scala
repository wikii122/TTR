package pl.enves.ttr.graphics

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.app.AlertDialog
import android.content.Context
import android.opengl.{GLES20, Matrix}
import android.opengl.GLSurfaceView.Renderer
import android.view.MotionEvent
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics.GameBoard
import pl.enves.ttr.graphics.models.DefaultGeometries
import pl.enves.ttr.logic._

import scala.util.{Failure, Success, Try}

/**
 * Manages the process of drawing the frame.
 */
class GameRenderer(context: Context, game: Game) extends Renderer with Logging {
  log("Creating")

  private[this] val resources = Resources()
  resources.addBitmapProvider(new DefaultTextures(context))
  resources.addGeometryProvider(new DefaultGeometries)
  private[this] val board = GameBoard(game, resources)
  private[this] var viewportWidth: Int = 1
  private[this] var viewportHeight: Int = 1

  def setCamera(): Unit = {
    //In case of inconsistent use of push and pop
    MVMatrix.clear()

    //Apply camera transformations
    //Matrix.setLookAtM(MVMatrix(), 0, 0.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    //or simply
    //Matrix.translateM(MVMatrix(), 0, 0.0f, 0.0f, -5.0f)
  }

  override def onDrawFrame(gl: GL10) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT)
    this.synchronized {
      setCamera()

      board.animate(1.0f)
      board.draw()
    }
  }

  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    this.synchronized {
      GLES20.glViewport(0, 0, width, height)
      viewportWidth = width
      viewportHeight = height

      Matrix.setIdentityM(PMatrix(), 0)
      if (height > width) {
        val ratio = height.toFloat / width.toFloat
        //Matrix.frustumM(PMatrix(), 0, -1.0f, 1.0f, -ratio, ratio, 3.0f, 7.0f)
        Matrix.orthoM(PMatrix(), 0, -1.0f, 1.0f, -ratio, ratio, -1.0f, 1.0f)
      } else {
        val ratio = width.toFloat / height.toFloat
        //Matrix.frustumM(PMatrix(), 0, -ratio, ratio, -1.0f, 1.0f, 3.0f, 7.0f)
        Matrix.orthoM(PMatrix(), 0, -ratio, ratio, -1.0f, 1.0f, -1.0f, 1.0f)
      }
    }
  }

  override def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    this.synchronized {
      GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
      GLES20.glClearDepthf(1.0f)
      GLES20.glEnable(GLES20.GL_DEPTH_TEST)
      GLES20.glDepthFunc(GLES20.GL_LEQUAL)
      GLES20.glDepthMask(true)
      GLES20.glEnable(GLES20.GL_CULL_FACE)
      GLES20.glCullFace(GLES20.GL_BACK)
      GLES20.glEnable(GLES20.GL_BLEND)
      GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

      resources.createOpenGLResources()
      board.updateResources()
    }
  }

  def onTouchEvent(e: MotionEvent): Boolean = {
    this.synchronized {
      if (e.getAction == MotionEvent.ACTION_DOWN) {
        val clickX = e.getX
        val clickY = viewportHeight - e.getY
        val viewport = Array(0, 0, viewportWidth, viewportHeight)

        setCamera()
        Try {
          board.click(clickX, clickY, viewport)
        } match {
          case Success(true) => if (game.finished) {
            val text = game.winner match {
              case Some(x) => s"Player $x wins"
              case None => "Game finished with a draw"
            }

            log(text)

            new AlertDialog.Builder(context).setMessage(text).create().show()
          }

          case Failure(err) => error(err.getMessage)
          case _ =>
        }
      }

      return true
    }
  }
}

object GameRenderer {
  def apply(context: Context with GameManager) = new GameRenderer(context, context.game)

  def apply(context: Context, game: Game) = new GameRenderer(context, game)
}
