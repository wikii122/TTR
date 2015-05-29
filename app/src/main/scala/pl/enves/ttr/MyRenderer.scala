package pl.enves.ttr

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView


class MyRenderer extends GLSurfaceView.Renderer {

  override def onDrawFrame(gl: GL10) {
   // TODO: Method stub
   GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
  }

  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
   //TODO: Method stub
   glViewport(0, 0, width, height)
  }

  override def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    //TODO: Method stub
    GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f)
  }

}