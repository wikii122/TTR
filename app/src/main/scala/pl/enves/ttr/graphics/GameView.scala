package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import pl.enves.ttr.logic.{Player, Game}
import pl.enves.ttr.utils.Logging

/**
 * Main view of the game.
 *
 * Takes responsibility for handling input from system and managing graphics rendering.
 */
class GameView(val context: Context) extends GLSurfaceView(context) with Logging {
  private[this] val renderer = GameRenderer()

  log("Creating")
  setEGLContextClientVersion(2)
  setRenderer(renderer)

  def startGame() = Game.start(Player.X)

  override def onTouchEvent(event: MotionEvent): Boolean = ???
}

object GameView {
  def apply(context: Context) = new GameView(context)
}
