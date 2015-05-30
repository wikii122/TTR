package pl.enves.ttr.graphics

import android.content.Context
import android.opengl.GLSurfaceView

class GameView(val context: Context) extends GLSurfaceView(context) {

}

object GameView {
  def apply(context: Context) = new GameView(context)
}
