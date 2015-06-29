package pl.enves.ttr.graphics.shaders

import android.opengl.GLES20
import pl.enves.ttr.graphics.Geometry

case class ColorShaderData(color: Array[Float]) extends AdditionalData

class ColorShader extends Shader {
  override def getVertexShaderCode: String =
    """
    uniform mat4 u_MVPMatrix;
    uniform vec4 u_Color;

    attribute vec4 a_Position;

    varying vec4 v_Color;

    void main() {
      gl_Position = u_MVPMatrix * a_Position;
      v_Color = u_Color;
    }
    """

  override def getFragmentShaderCode: String =
    """
    precision highp float;

    varying vec4 v_Color;

    void main() {
      gl_FragColor = v_Color;
    }
    """

  override def draw(model: Geometry, data: AdditionalData) {
    val mvpMatrix = makeMVPMatrix
    val vertexBuffer: Int = model.getVBOS.positions
    val colorBuffer: Int = model.getVBOS.colors

    GLES20.glUseProgram(program)
    checkGlError("glUseProgram")

    //Get handlers to attributes
    val mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
    checkGlError("glGetAttribLocation")

    // Get handlers to uniforms
    val mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
    checkGlError("glGetUniformLocation")

    val mColorHandle = GLES20.glGetUniformLocation(program, "u_Color")
    checkGlError("glGetUniformLocation")

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
    checkGlError("glBindBuffer")

    GLES20.glEnableVertexAttribArray(mPositionHandle)
    checkGlError("glEnableVertexAttribArray")

    GLES20.glVertexAttribPointer(mPositionHandle, COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)
    checkGlError("glVertexAttribPointer")

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    checkGlError("glBindBuffer")

    // Apply color
    GLES20.glUniform4fv(mColorHandle, 1, data.asInstanceOf[ColorShaderData].color, 0)
    checkGlError("glUniform4fv")

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
    checkGlError("glUniformMatrix4fv")

    // Draw
    model.draw()
    checkGlError("draw")

    // Disable attributes
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    checkGlError("glDisableVertexAttribArray")
    GLES20.glDisableVertexAttribArray(mColorHandle)
    checkGlError("glDisableVertexAttribArray")
  }
}
