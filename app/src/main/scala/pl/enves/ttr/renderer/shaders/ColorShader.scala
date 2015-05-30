package pl.enves.ttr.renderer.shaders

import android.opengl.GLES20
import pl.enves.ttr.renderer.Model3d

class ColorShader extends Shader {
  override def getVertexShaderCode: String = {
    var r: String = ""
    // the coordinates of the objects that use this vertex shader
    r += "uniform mat4 u_MVPMatrix;"

    r += "attribute vec4 a_Position;"
    r += "attribute vec4 a_Color;"
    r += "varying vec4 v_Color;"
    r += "void main() {"
    // the matrix must be included as a modifier of gl_Position
    r += "  gl_Position = u_MVPMatrix * a_Position;"
    r += "  v_Color = a_Color;"
    r += "}"
    r
  }

  override def getFragmentShaderCode: String = {
    var r: String = ""
    r += "precision mediump float;"
    r += "varying vec4 v_Color;"
    r += "void main() {"
    r += "  gl_FragColor = v_Color;"
    r += "}"
    r
  }

  override def drawBuffers(mvpMatrix: Array[Float], model: Model3d) {
    val vertexBuffer = model.getPositionsBuffer
    val colorBuffer = model.getColorsBuffer

    GLES20.glUseProgram(program)
    checkGlError("glUseProgram")

    //Get handlers to attributes

    val mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
    checkGlError("glGetAttribLocation")

    val mColorHandle = GLES20.glGetAttribLocation(program, "a_Color")
    checkGlError("glGetAttribLocation")

    // Get handlers to uniforms
    val mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
    checkGlError("glGetUniformLocation")

    // Apply positions
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
    checkGlError("glBindBuffer")

    GLES20.glEnableVertexAttribArray(mPositionHandle)
    checkGlError("glEnableVertexAttribArray")

    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, 0)
    checkGlError("glVertexAttribPointer")

    // Apply colors
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBuffer)
    checkGlError("glBindBuffer")

    GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, 0)
    checkGlError("glVertexAttribPointer")

    GLES20.glEnableVertexAttribArray(mColorHandle)
    checkGlError("glEnableVertexAttribArray")

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    checkGlError("glBindBuffer")

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
    checkGlError("glUniformMatrix4fv")

    // Draw
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, model.getNumVertex)
    checkGlError("glDrawArrays")

    // Disable attributes
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    checkGlError("glDisableVertexAttribArray")
    GLES20.glDisableVertexAttribArray(mColorHandle)
    checkGlError("glDisableVertexAttribArray")
  }
}
