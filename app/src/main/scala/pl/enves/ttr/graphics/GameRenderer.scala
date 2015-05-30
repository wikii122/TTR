package pl.enves.ttr.graphics

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLSurfaceView.Renderer

/**
 * Manages the process of drawing the frame.
 */
class GameRenderer extends Renderer {
  override def onSurfaceChanged(gl: GL10, width: Int, height: Int): Unit = ???

  override def onSurfaceCreated(gl: GL10, config: EGLConfig): Unit = ???

  override def onDrawFrame(gl: GL10): Unit = ???
}

object GameRenderer {
  def apply() = new GameRenderer
}