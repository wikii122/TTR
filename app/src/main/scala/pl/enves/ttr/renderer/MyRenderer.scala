package pl.enves.ttr.renderer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20.glViewport
import android.opengl.{GLES20, GLSurfaceView, Matrix}

class MyRenderer extends GLSurfaceView.Renderer {

  private[this] var resources: Option[Resources] = None
  private[this] var scene: Option[Scene] = None

  override def onDrawFrame(gl: GL10) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    GLES20.glEnable(GLES20.GL_CULL_FACE)
    gl.glCullFace(GL10.GL_BACK)

    //In case of inconsistent use of push and pop
    MVMatrix.clear()

    //Apply camera transformations
    //Matrix.setLookAtM(MVMatrix(), 0, 0.0f, 0.0f, -5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    //or simply
    //Matrix.translateM(MVMatrix(), 0, 0.0f, 0.0f, 0.0f)

    scene.get.draw()
  }

  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    //TODO: Method stub
    glViewport(0, 0, width, height)

    Matrix.setIdentityM(PMatrix(), 0)
    if (height > width) {
      val ratio = height.toFloat / width.toFloat
      //Matrix.frustumM(pMatrix, 0, -1.0f, 1.0f, -ratio, ratio, 3.0f, 7.0f)
      Matrix.orthoM(PMatrix(), 0, -1.0f, 1.0f, -ratio, ratio, -1.0f, 1.0f)
    } else {
      val ratio = width.toFloat / height.toFloat
      //Matrix.frustumM(pMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 3.0f, 7.0f)
      Matrix.orthoM(PMatrix(), 0, -ratio, ratio, -1.0f, 1.0f, -1.0f, 1.0f)
    }
  }

  override def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    //TODO: Method stub
    GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f)

    resources = Some(Resources())
    scene = Some(Scene(resources.get))
  }
}