package pl.enves.ttr.graphics.shaders

import android.opengl.GLES20
import pl.enves.ttr.graphics.{MatrixStack, Geometry}

class ColorShader extends Shader {

  // Get handlers to attributes
  val mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")

  // Get handlers to uniforms
  val mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
  val mColorHandle = GLES20.glGetUniformLocation(program, "u_Color")

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

  /**
   * (color: Array[Float])
   */
  override type dataType = (Float, Float, Float, Float)

  override def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack, model: Geometry, data: dataType) {
    val mvpMatrix = makeMVPMatrix(mvMatrix, pMatrix)
    val vertexBuffer: Int = model.getVBOS.positions

    GLES20.glUseProgram(program)

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)

    GLES20.glEnableVertexAttribArray(mPositionHandle)

    GLES20.glVertexAttribPointer(mPositionHandle, COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // Apply color
    GLES20.glUniform4f(mColorHandle, data._1, data._2, data._3, data._4)

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

    // Draw
    model.draw()

    // Disable attributes
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    GLES20.glDisableVertexAttribArray(mColorHandle)
  }
}

