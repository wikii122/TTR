package pl.enves.ttr.renderer

import android.opengl.Matrix

import pl.enves.ttr.renderer.shaders._

class Scene(resources: Resources) {

  def draw(pMatrix: Array[Float], mvMatrix: Array[Float]) {
    val mvpMatrix = new Array[Float](16)
    Matrix.scaleM(mvMatrix, 0, 1.0f, 1.0f, 1.0f)

    val ms = resources.getShader(resources.ID_SHADER_MANDEL).asInstanceOf[MandelShader]
    val cs = resources.getShader(resources.ID_SHADER_COLOR).asInstanceOf[ColorShader]

    val rm = resources.getModel3d(resources.ID_MODEL_RECT)
    val tm = resources.getModel3d(resources.ID_MODEL_TRIANGLE)

    Matrix.translateM(mvMatrix, 0, -0.5f, -0.5f, 0.0f)
    Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)
    ms.drawBuffers(mvpMatrix, rm)

    Matrix.translateM(mvMatrix, 0, 1.0f, 0.0f, 0.0f)
    Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)
    ms.drawBuffers(mvpMatrix, tm)

    Matrix.translateM(mvMatrix, 0, 0.0f, 1.0f, 0.0f)
    Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)
    cs.drawBuffers(mvpMatrix, rm)

    Matrix.translateM(mvMatrix, 0, -1.0f, 0.0f, 0.0f)
    Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0)
    cs.drawBuffers(mvpMatrix, tm)
  }
}
