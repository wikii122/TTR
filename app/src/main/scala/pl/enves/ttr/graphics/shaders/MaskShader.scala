package pl.enves.ttr.graphics.shaders

import android.opengl.GLES20
import pl.enves.androidx.color.ColorTypes._
import pl.enves.ttr.graphics.MatrixStack
import pl.enves.ttr.graphics.geometry.Geometry

/**
 * From now on, shape is defined only by alpha channel.
 * This works well with glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA),
 * and is somewhat compatible with PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN),
 * that we are using to style some UI components.
 * ( PorterDuff.Mode.SRC_IN is [Sa*Da, Sc*Da], whereas this shader implements [Sa*Da, Sc] )
 * ( S = color, D = mask, c = [red, green, blue], a = alpha )
 * ( Calculations cannot be the same because mask is interpolated earlier here )
 */

class MaskShader extends Shader {

  // Get handlers to attributes
  private[this] val positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
  private[this] val texCoordHandle = GLES20.glGetAttribLocation(program, "a_MaskTexCoord")

  // Get handlers to uniforms
  private[this] val MVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
  private[this] val samplerHandle = GLES20.glGetUniformLocation(program, "u_Sampler")
  private[this] val colorHandle = GLES20.glGetUniformLocation(program, "u_Color")

  override def getVertexShaderCode: String =
    """
    uniform mat4 u_MVPMatrix;

    attribute vec4 a_Position;
    attribute vec2 a_MaskTexCoord;

    varying vec2 v_MaskTexCoord;

    void main() {
      gl_Position = u_MVPMatrix * a_Position;
      v_MaskTexCoord = a_MaskTexCoord;
    }
    """

  override def getFragmentShaderCode: String =
    """
    #ifdef GL_ES
      precision highp float;
    #endif

    uniform vec4 u_Color;
    uniform sampler2D u_Sampler;

    varying vec2 v_MaskTexCoord;

    void main(void) {
      vec4 mask = texture2D(u_Sampler, vec2(v_MaskTexCoord.s, v_MaskTexCoord.t));
      vec4 color;
      color.a = u_Color.a * mask.a;
      color.r = u_Color.r;
      color.g = u_Color.g;
      color.b = u_Color.b;

      //gl_FragColor = clamp(color, 0.0, 1.0);
      gl_FragColor = color;
    }
    """

  def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack, model: Geometry, color: ColorArray, mask: Int) {
    makeMVPMatrix(mvMatrix, pMatrix)

    val positionsBuffer = model.buffers.positions
    val texCoordsBuffer = model.buffers.texCoords

    GLES20.glUseProgram(program)

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionsBuffer)

    GLES20.glEnableVertexAttribArray(positionHandle)

    GLES20.glVertexAttribPointer(positionHandle, Geometry.PositionSize, GLES20.GL_FLOAT, false, 0, 0)

    // Apply texture coordinates
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordsBuffer)

    GLES20.glVertexAttribPointer(texCoordHandle, Geometry.TexCoordSize, GLES20.GL_FLOAT, false, 0, 0)

    GLES20.glEnableVertexAttribArray(texCoordHandle)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0)

    //Apply Colors
    GLES20.glUniform4fv(colorHandle, 1, color, 0)

    // Set the active texture unit to texture unit 0.
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

    // Bind the texture to this unit.
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mask)

    // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
    GLES20.glUniform1i(samplerHandle, 0)

    // Draw
    model.draw()

    // Disable attributes
    GLES20.glDisableVertexAttribArray(positionHandle)
    GLES20.glDisableVertexAttribArray(texCoordHandle)

    // Disable
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
  }
}
