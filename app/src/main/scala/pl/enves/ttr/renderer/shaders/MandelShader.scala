package pl.enves.ttr.renderer.shaders

import android.opengl.GLES20
import pl.enves.ttr.renderer.Model3d

class MandelShader extends Shader {
	override def getVertexShaderCode: String = {
		var r: String = ""
		r += "uniform mat4 u_MVPMatrix;"

    r += "attribute vec4 a_Position;"
    r += "varying vec4 v_Position;"
    r += "void main() {"

    r += "  gl_Position = u_MVPMatrix * a_Position;"
    r += "  v_Position = a_Position;"
    r += "}"
    r
	}

	override def getFragmentShaderCode: String = {
    var r: String = ""
		r += "precision mediump float;"
    r += "varying vec4 v_Position;"

    r += "void main(void)"
    r += "{"
    r += "  float MaxIterations = 10.0;"
    r += "  float Xcenter = 0.0;"
    r += "  float Ycenter = 0.5;"
    r += "  vec3  InnerColor = vec3(0.0, 0.0, 0.0);"
    r += "  vec3  OuterColor1 = vec3(0.0, 0.0, 1.0);"
    r += "  vec3  OuterColor2 = vec3(0.0, 1.0, 0.0);"
    r += "  float RangeColor = 0.2;"
    r += "  float Zoom = 3.0;"
    r += "  float real  = ((-v_Position.y) * Zoom) - Ycenter;"
    r += "  float imag  = ((v_Position.x) * Zoom) - Xcenter;"
    r += "  float Creal = real;"   // Change this line...
    r += "  float Cimag = imag;"   // ...and this one to get a Julia set

    r += "  float r2 = 0.0;"
    r += "  float iter;"
    r += "  float tempreal;"
    r += "  for (iter = 0.0; iter < MaxIterations && r2 < 4.0; iter = iter + 1.0)"
    r += "  {"
    r += "    tempreal = real;"
    r += "    real = (tempreal * tempreal) - (imag * imag) + Creal;"
    r += "    imag = 2.0 * tempreal * imag + Cimag;"
    r += "    r2   = (real * real) + (imag * imag);"
    r += "  }"

    // Base the color on the number of iterations
    r += "  vec3 color;"

    r += "  if (r2 < 4.0)"
    r += "  {"
    r += "    color = InnerColor;"
    r += "  }else{"
    r += "    color = mix(OuterColor1, OuterColor2, fract(iter * RangeColor ));"
    r += "  }"

    r += "  gl_FragColor = vec4(color, 1.0);"
    r += "}"
    r
	}

  override def drawBuffers(mvpMatrix: Array[Float], model: Model3d) {
		val vertexBuffer = model.getPositionsBuffer

    GLES20.glUseProgram(program)
		checkGlError("glUseProgram")

		//Get handlers to attributes
    val mPositionHandle: Int = GLES20.glGetAttribLocation(program, "a_Position")
		checkGlError("glGetAttribLocation")

		// Get handlers to uniforms
    val mMVPMatrixHandle: Int = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
		checkGlError("glGetUniformLocation")

    // Apply the MVP matrix
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
    checkGlError("glUniformMatrix4fv")

		// Apply positions
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
		checkGlError("glBindBuffer")

		GLES20.glEnableVertexAttribArray(mPositionHandle)
		checkGlError("glEnableVertexAttribArray")

		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, 0)
		checkGlError("glVertexAttribPointer")

		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
		checkGlError("glBindBuffer")

		// Draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, model.getNumVertex)
		checkGlError("glDrawArrays")
		
		// Disable attributes
		GLES20.glDisableVertexAttribArray(mPositionHandle)
		checkGlError("glDisableVertexAttribArray")
	}
}
