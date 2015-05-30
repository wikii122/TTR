package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

/**
 * Main view of the game.
 *
 * Takes responsibility for handling input from system and managing graphics rendering.
 */
class GameView(val context: Context) extends GLSurfaceView(context) {
  private[this] val renderer = GameRenderer()

  override def onTouchEvent(event: MotionEvent): Boolean = ???
}

object GameView {
  def apply(context: Context) = new GameView(context)
}
