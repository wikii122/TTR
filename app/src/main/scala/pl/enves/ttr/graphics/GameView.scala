package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView

/**
 * Main view of the game.
 *
 * Takes responsibility for handling input from system and managing graphics rendering.
 */
class GameView(val context: Context) extends GLSurfaceView(context) {

}

object GameView {
  def apply(context: Context) = new GameView(context)
}
