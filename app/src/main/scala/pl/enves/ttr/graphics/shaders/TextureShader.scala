package pl.enves.ttr.graphics.shaders

import android.opengl.GLES20
import pl.enves.ttr.graphics.Geometry

class TextureShader extends Shader {

  override def getVertexShaderCode: String =
    """
    uniform mat4 u_MVPMatrix;

    attribute vec4 a_Position;
    attribute vec2 a_TexCoord;

    varying vec2 v_TexCoord;

    void main() {
      gl_Position = u_MVPMatrix * a_Position;
      v_TexCoord = a_TexCoord;
    }
    """

  override def getFragmentShaderCode: String =
    """
    #ifdef GL_ES
      precision highp float;
    #endif

    varying vec2 v_TexCoord;

    uniform sampler2D uSampler;

    void main(void) {
      gl_FragColor = texture2D(uSampler, vec2(v_TexCoord.s, v_TexCoord.t));
    }
    """

  /**
   * texture: Int
   */
  override type dataType = Int

  override def draw(model: Geometry, data: dataType) {
    val mvpMatrix = makeMVPMatrix

    val positionsBuffer = model.getVBOS.positions
    val texCoordsBuffer = model.getVBOS.texCoords

    GLES20.glUseProgram(program)
    checkGlError("glUseProgram")

    // Get handlers to attributes

    val mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
    checkGlError("glGetAttribLocation")

    val mTexCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
    checkGlError("glGetAttribLocation")

    // Get handlers to uniforms
    val mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
    checkGlError("glGetUniformLocation")

    val mSamplerHandle = GLES20.glGetUniformLocation(program, "u_Sampler")
    checkGlError("glGetUniformLocation")

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionsBuffer)
    checkGlError("glBindBuffer")

    GLES20.glEnableVertexAttribArray(mPositionHandle)
    checkGlError("glEnableVertexAttribArray")

    GLES20.glVertexAttribPointer(mPositionHandle, COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)
    checkGlError("glVertexAttribPointer")

    // Apply texture coordinates
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordsBuffer)
    checkGlError("glBindBuffer")

    GLES20.glVertexAttribPointer(mTexCoordHandle, TEX_COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)
    checkGlError("glVertexAttribPointer")

    GLES20.glEnableVertexAttribArray(mTexCoordHandle)
    checkGlError("glEnableVertexAttribArray")

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    checkGlError("glBindBuffer")

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
    checkGlError("glUniformMatrix4fv")

    // Set the active texture unit to texture unit 0.
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

    // Bind the texture to this unit.
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, data)

    // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
    GLES20.glUniform1i(mSamplerHandle, 0)

    // Draw
    model.draw()
    checkGlError("draw")

    // Disable attributes
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    checkGlError("glDisableVertexAttribArray")
    GLES20.glDisableVertexAttribArray(mTexCoordHandle)
    checkGlError("glDisableVertexAttribArray")

    // Disable
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
  }
}
