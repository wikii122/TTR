package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import pl.enves.ttr.logic.{Player, StandardGame}
import pl.enves.ttr.utils.Logging

/**
 * Main view of the game.
 *
 * Takes responsibility for handling input from system and managing graphics rendering.
 */
class GameView(val context: Context) extends GLSurfaceView(context) with Logging {
  private[this] val renderer = GameRenderer(context)

  log("Creating")
  setEGLConfigChooser(true)   //true, cause we need depth buffer
  setEGLContextClientVersion(2)

  setRenderer(renderer)

  setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
  //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)

  def startGame() = StandardGame.start(Player.X)

  override def onTouchEvent(e: MotionEvent): Boolean = {
    return renderer.onTouchEvent(e)
  }
}

object GameView {
  def apply(context: Context) = new GameView(context)
}
