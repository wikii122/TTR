package pl.enves.ttr.renderer.shaders

import android.opengl.GLES20
import pl.enves.ttr.renderer.Model3d

class TextureShader extends Shader {

  override def getVertexShaderCode: String = {
    ""
  }

  override def getFragmentShaderCode: String = {
    ""
  }

  override def drawBuffers(mvpMatrix: Array[Float], model: Model3d) {
    GLES20.glUseProgram(program)
    checkGlError("glUseProgram")
  }
}
