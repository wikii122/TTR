package pl.enves.ttr.graphics.shaders

/**
 * Every shader needs some code to bind data from graphic or user memory
 * In simple case:
 * constant MVP matrix - float[16]
 * per-vertex: position and (color or texture coords)
 */

import android.opengl.{GLES20, Matrix}
import android.util.Log
import pl.enves.ttr.graphics.MatrixStack
import pl.enves.ttr.graphics.geometry.Geometry

abstract class Shader {
  // prepare shaders and OpenGL program
  private[this] val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShaderCode)
  private[this] val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderCode)

  protected val program = GLES20.glCreateProgram() // create empty OpenGL Program
  checkGlError("glCreateProgram")

  GLES20.glAttachShader(program, vertexShader)
  checkGlError("glAttachShader")

  GLES20.glAttachShader(program, fragmentShader)
  checkGlError("glAttachShader")

  GLES20.glLinkProgram(program)
  checkGlError("glLinkProgram")

  // Get the link status.
  private[this] val linkStatus = new Array[Int](1)
  GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)

  // If the link failed, delete the program.
  if (linkStatus(0) == 0) {
    val s = GLES20.glGetProgramInfoLog(program)
    Log.e("Shader", "LinkFailed:" + s)
    GLES20.glDeleteProgram(program)
  }

  if (!GLES20.glIsProgram(program)) {
    Log.e("Shader", "NoProgram")
  }

  protected val mvpMatrix = new Array[Float](16)

  protected def getVertexShaderCode: String

  protected def getFragmentShaderCode: String

  private def checkGlError(glOperation: String) {
    var error: Int = GLES20.glGetError()
    while (error != GLES20.GL_NO_ERROR) {
      Log.e("Shader", glOperation + ": glError " + error)
      error = GLES20.glGetError()
    }
  }

  private def loadShader(shaderType: Int, shaderCode: String): Int = {
    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    val shader = GLES20.glCreateShader(shaderType)
    checkGlError("glCreateShader")

    // add the source code to the shader and compile it
    GLES20.glShaderSource(shader, shaderCode)
    checkGlError("glShaderSource")

    GLES20.glCompileShader(shader)
    checkGlError("glCompileShader")

    val s = GLES20.glGetShaderInfoLog(shader)
    Log.d("Shader", "CompilationLog:" + s)

    return shader
  }

  protected def makeMVPMatrix(mvMatrix: MatrixStack, pMatrix: MatrixStack) = {
    Matrix.multiplyMM(mvpMatrix, 0, pMatrix.get(), 0, mvMatrix.get(), 0)
  }
}
