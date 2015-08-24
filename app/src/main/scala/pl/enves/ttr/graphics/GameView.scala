package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics.themes.ThemeId
import ThemeId.ThemeId
import pl.enves.ttr.logic.{GameManager, Player, StandardGame}

/**
 * Main view of the game.
 *
 * Takes responsibility for handling input from system and managing graphics rendering.
 */
class GameView(val context: Context with GameManager) extends GLSurfaceView(context) with Logging {
  private[this] val renderer = GameRenderer(context)

  log("Creating")
  setEGLConfigChooser(true)   //true, cause we need depth buffer
  setEGLContextClientVersion(2)

  setRenderer(renderer)

  setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
  //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)

  def startGame() = context.game.start(Player.X)

  def setTheme(theme: ThemeId) = renderer.setTheme(theme)

  override def onTouchEvent(e: MotionEvent): Boolean = renderer.onTouchEvent(e)
}

object GameView {
  def apply(context: Context with GameManager) = new GameView(context)
}
