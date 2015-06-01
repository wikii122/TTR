package pl.enves.ttr.renderer

import android.opengl.Matrix

import pl.enves.ttr.renderer.shaders._

class Scene(resources: Resources) {

  implicit def shaderToColorShader(shader: Shader): ColorShader = shader.asInstanceOf[ColorShader]

  implicit def shaderToMandelShader(shader: Shader): MandelShader = shader.asInstanceOf[MandelShader]

  def draw() {

    MVMatrix.push()

    Matrix.scaleM(MVMatrix(), 0, 1.0f, 1.0f, 1.0f)

    val ms = resources.getShader(resources.ShaderId.Mandel)
    val cs = resources.getShader(resources.ShaderId.Color)

    val rm = resources.getModel3d(resources.ModelId.Rectangle)
    val tm = resources.getModel3d(resources.ModelId.Triangle)

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.5f, -0.5f, 0.0f)
    ms.drawBuffers(rm)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.5f, -0.5f, 0.0f)
    ms.drawBuffers(tm)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.5f, 0.5f, 0.0f)
    cs.drawBuffers(rm)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.5f, 0.5f, 0.0f)
    cs.drawBuffers(tm)
    MVMatrix.pop()

    MVMatrix.pop()
  }
}

object Scene {
  def apply(resources: Resources) = new Scene(resources)
}