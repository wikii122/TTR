package pl.enves.ttr.graphics.shaders

import android.opengl.GLES20
import pl.enves.ttr.graphics.{MatrixStack, Geometry}

/**
 * output.rgba = color1*mask.r + color2*mask.g + color3*mask.b
 */

class MaskShader extends Shader {

  // Get handlers to attributes
  val positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
  val texCoordHandle = GLES20.glGetAttribLocation(program, "a_MaskTexCoord")

  // Get handlers to uniforms
  val MVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
  val samplerHandle = GLES20.glGetUniformLocation(program, "u_Sampler")
  val color1Handle = GLES20.glGetUniformLocation(program, "u_Color1")
  val color2Handle = GLES20.glGetUniformLocation(program, "u_Color2")
  val color3Handle = GLES20.glGetUniformLocation(program, "u_Color3")

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

    uniform vec4 u_Color1;
    uniform vec4 u_Color2;
    uniform vec4 u_Color3;

    varying vec2 v_MaskTexCoord;

    uniform sampler2D uSampler;

    void main(void) {
      vec4 mask = texture2D(uSampler, vec2(v_MaskTexCoord.s, v_MaskTexCoord.t));
      vec4 color;
      color.r = u_Color1.r*mask.r + u_Color2.r*mask.g + u_Color3.r*mask.b;
      color.g = u_Color1.g*mask.r + u_Color2.g*mask.g + u_Color3.g*mask.b;
      color.b = u_Color1.b*mask.r + u_Color2.b*mask.g + u_Color3.b*mask.b;
      color.a = u_Color1.a*mask.r + u_Color2.a*mask.g + u_Color3.a*mask.b;

      gl_FragColor = clamp(color, 0.0, 1.0);
      //gl_FragColor = mask;
    }
    """

  /**
   * (color1rgba, color2rgba, color3rgba, mask)
   */
  override type dataType = (Array[Float], Array[Float], Array[Float], Int)

  override def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack, model: Geometry, data: dataType) {
    makeMVPMatrix(mvMatrix, pMatrix)

    val positionsBuffer = model.getVBOS.positions
    val texCoordsBuffer = model.getVBOS.texCoords

    GLES20.glUseProgram(program)

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionsBuffer)

    GLES20.glEnableVertexAttribArray(positionHandle)

    GLES20.glVertexAttribPointer(positionHandle, COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)

    // Apply texture coordinates
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texCoordsBuffer)

    GLES20.glVertexAttribPointer(texCoordHandle, TEX_COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)

    GLES20.glEnableVertexAttribArray(texCoordHandle)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0)

    //Apply Colors
    GLES20.glUniform4fv(color1Handle, 1, data._1, 0)

    GLES20.glUniform4fv(color2Handle, 1, data._2, 0)

    GLES20.glUniform4fv(color3Handle, 1, data._3, 0)

    // Set the active texture unit to texture unit 0.
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

    // Bind the texture to this unit.
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, data._4)

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