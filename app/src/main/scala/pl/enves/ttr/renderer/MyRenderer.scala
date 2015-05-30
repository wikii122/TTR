package pl.enves.ttr.renderer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20.glViewport
import android.opengl.{GLES20, GLSurfaceView, Matrix}


class MyRenderer extends GLSurfaceView.Renderer {

  var pMatrix = new Array[Float](16)
  var mvMatrix = new Array[Float](16)

  var resources: Resources = _
  var scene: Scene = _

  override def onDrawFrame(gl: GL10) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    //GLES20.glEnable(GLES20.GL_CULL_FACE)
    //gl.glCullFace(GL10.GL_BACK)

    //Matrix.setLookAtM(mvMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    Matrix.setIdentityM(mvMatrix, 0)
    Matrix.translateM(mvMatrix, 0, 0.0f, 0.0f, -105.0f)
    scene.draw(pMatrix, mvMatrix)
  }

  override def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    //TODO: Method stub
    glViewport(0, 0, width, height)
    if(height>width) {
      val ratio = height.toFloat / width.toFloat
      Matrix.frustumM(pMatrix, 0, -1.0f, 1.0f, -ratio, ratio, 103.0f, 107.0f)
    }else{
      val ratio = width.toFloat / height.toFloat
      Matrix.frustumM(pMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 103.0f, 107.0f)
    }
  }

  override def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    //TODO: Method stub
    GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f)

    resources = new Resources
    scene = new Scene(resources)
  }
}