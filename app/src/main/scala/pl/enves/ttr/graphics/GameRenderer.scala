package pl.enves.ttr.graphics

import java.security.InvalidParameterException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.app.AlertDialog
import android.content.Context
import android.opengl.{GLES20, Matrix}
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.view.MotionEvent
import pl.enves.ttr.logic.{Game, GameWon, GameFinished, FieldTaken}
import pl.enves.ttr.utils.Logging

import scala.util.{Failure, Success, Try}

/**
 * Manages the process of drawing the frame.
 */
class GameRenderer(context: Context) extends Renderer with Logging {
  log("Creating")

  private[this] lazy val board: GameBoard = GameBoard(Resources(context))

  var viewportWidth: Int = 1
  var viewportHeight: Int = 1

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

      //board.get.animate()
      board.draw(DrawReason.Render)
    }
  }

  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    this.synchronized {
      glViewport(0, 0, width, height)
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
      GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
      GLES20.glClearDepthf(1.0f)
      GLES20.glEnable(GLES20.GL_DEPTH_TEST)
      GLES20.glDepthFunc(GLES20.GL_LEQUAL)
      GLES20.glDepthMask(true)
      GLES20.glEnable(GLES20.GL_CULL_FACE)
      GLES20.glCullFace(GLES20.GL_BACK)
      GLES20.glEnable(GLES20.GL_BLEND)
      GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }
  }

  def onTouchEvent(e: MotionEvent): Boolean = {
    this.synchronized {
      if (e.getAction == MotionEvent.ACTION_DOWN) {
        ClickInfo.X = e.getX
        ClickInfo.Y = viewportHeight - e.getY
        ClickInfo.viewport = Array(0, 0, viewportWidth, viewportHeight)

        setCamera()
        Try {
          board.draw(DrawReason.Click)
        } match {
          case _: Success[Unit] => if (Game.finished) {
            val text = Game.winner match {
              case Some(x) => s"Player $x wins"
              case None => "Game finished with a draw"
            }

            log(text)

            new AlertDialog.Builder(context).setMessage(text).create().show()
          }

          case Failure(err) => error(err.getMessage)
        }
      }

      return true
    }
  }
}

object GameRenderer {
  def apply(context: Context) = new GameRenderer(context)
}
