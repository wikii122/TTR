package pl.enves.ttr.renderer

import android.opengl.Matrix

import pl.enves.ttr.renderer.shaders._

class Scene(resources: Resources) {

  implicit def shaderToColorShader(shader: Shader): ColorShader = shader.asInstanceOf[ColorShader]

  implicit def shaderToTextureShader(shader: Shader): TextureShader = shader.asInstanceOf[TextureShader]

  var angle = 0.0f

  def animate(): Unit = {
    angle+=0.5f
    if(angle>=360.0f) {
      angle = 0.0f
    }
  }

  def draw() {

    MVMatrix.push()

    Matrix.scaleM(MVMatrix(), 0, 1.0f, 1.0f, 1.0f)

    val cs = resources.getShader(resources.ShaderId.Color)
    val ts = resources.getShader(resources.ShaderId.Texture)

    val rm = resources.getGeometry(resources.ModelId.Rectangle)
    val tm = resources.getGeometry(resources.ModelId.Triangle)
    val cm = resources.getGeometry(resources.ModelId.Cube)

    val tt1 = resources.getTexture(resources.TextureId.Test1)
    val tt2 = resources.getTexture(resources.TextureId.Test2)

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.5f, -0.5f, 0.0f)
    ts.draw(rm, tt1)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.5f, -0.5f, 0.0f)
    ts.draw(tm, tt2)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.5f, 0.5f, 0.0f)
    cs.draw(rm)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.5f, 0.5f, 0.0f)
    cs.draw(tm)
    MVMatrix.pop()

    Matrix.translateM(MVMatrix(), 0, 0.0f, 0.0f, 0.5f)

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.5f, 0.5f, 0.0f)
    Matrix.rotateM(MVMatrix(), 0, angle, 1.0f, 1.0f, 1.0f)
    Matrix.scaleM(MVMatrix(), 0, 0.5f, 0.5f, 0.5f)
    cs.draw(cm)
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.5f, -0.5f, 0.0f)
    Matrix.rotateM(MVMatrix(), 0, angle, 1.0f, 1.0f, 1.0f)
    Matrix.scaleM(MVMatrix(), 0, 0.5f, 0.5f, 0.5f)
    ts.draw(cm, tt2)
    MVMatrix.pop()

    MVMatrix.pop()
  }
}

object Scene {
  def apply(resources: Resources) = new Scene(resources)
}