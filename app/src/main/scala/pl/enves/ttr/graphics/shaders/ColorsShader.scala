package pl.enves.ttr.graphics.shaders

import android.opengl.GLES20
import pl.enves.ttr.graphics.{MatrixStack, Geometry}

class ColorsShader extends Shader {

  // Get handlers to attributes
  val mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
  val mColorHandle = GLES20.glGetAttribLocation(program, "a_Color")

  // Get handlers to uniforms
  val mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")

  override def getVertexShaderCode: String =
    """
    uniform mat4 u_MVPMatrix;

    attribute vec4 a_Position;
    attribute vec4 a_Color;

    varying vec4 v_Color;

    void main() {
      gl_Position = u_MVPMatrix * a_Position;
      v_Color = a_Color;
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

  override type dataType = Null

  override def draw(mvMatrix: MatrixStack, pMatrix: MatrixStack, model: Geometry, data: dataType) {
    val mvpMatrix = makeMVPMatrix(mvMatrix, pMatrix)
    val vertexBuffer:Int = model.getVBOS.positions
    val colorBuffer:Int = model.getVBOS.colors

    GLES20.glUseProgram(program)

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)

    GLES20.glEnableVertexAttribArray(mPositionHandle)

    GLES20.glVertexAttribPointer(mPositionHandle, COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)

    // Apply colors
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBuffer)

    GLES20.glVertexAttribPointer(mColorHandle, COLOR_SIZE, GLES20.GL_FLOAT, false, 0, 0)

    GLES20.glEnableVertexAttribArray(mColorHandle)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

    // Draw
    model.draw()

    // Disable attributes
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    GLES20.glDisableVertexAttribArray(mColorHandle)
  }
}
