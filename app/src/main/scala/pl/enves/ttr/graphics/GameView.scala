package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import pl.enves.androidx.Logging
import pl.enves.ttr.logic.{GameManager, Player}
import pl.enves.ttr.utils.themes.Theme

/**
 * Main view of the game.
 *
 * Takes responsibility for handling input from system and managing graphics rendering.
 */
class GameView(val context: Context with GameManager, onEnd: Option[Player.Value] => Unit) extends GLSurfaceView(context) with Logging {
  private[this] val renderer = GameRenderer(context, onEnd)

  log("Creating")
  setEGLConfigChooser(true) //true, cause we need depth buffer
  setEGLContextClientVersion(2)

  setRenderer(renderer)

  setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)

  def startGame() = context.game.start(Player.X)

  def setTheme(theme: Theme) = renderer.setTheme(theme)

  override def onTouchEvent(e: MotionEvent): Boolean = renderer.onTouchEvent(e)
}

object GameView {
  def apply(context: Context with GameManager, onEnd: Option[Player.Value] => Unit) = new GameView(context, onEnd)
}
