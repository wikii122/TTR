package pl.enves.ttr.renderer.shaders

import android.opengl.GLES20
import pl.enves.ttr.renderer.Model3d

class MandelShader extends Shader {
  override def getVertexShaderCode: String =
    """
		uniform mat4 u_MVPMatrix;

    attribute vec4 a_Position;

    varying vec4 v_Position;

    void main() {
      gl_Position = u_MVPMatrix * a_Position;
      v_Position = a_Position;
    }
    """

  override def getFragmentShaderCode: String =
    """
		precision mediump float;

    varying vec4 v_Position;

    void main(void)
    {
      float MaxIterations = 10.0;
      float Xcenter = 0.0;
      float Ycenter = 0.5;
      vec3  InnerColor = vec3(0.0, 0.0, 0.0);
      vec3  OuterColor1 = vec3(0.0, 0.0, 1.0);
      vec3  OuterColor2 = vec3(0.0, 1.0, 0.0);
      float RangeColor = 0.2;
      float Zoom = 3.0;
      float real  = ((-v_Position.y) * Zoom) - Ycenter;
      float imag  = ((v_Position.x) * Zoom) - Xcenter;
      float Creal = real;   // Change this line...
      float Cimag = imag;   // ...and this one to get a Julia set

      float r2 = 0.0;
      float iter;
      float tempreal;
      for (iter = 0.0; iter < MaxIterations && r2 < 4.0; iter = iter + 1.0)
      {
        tempreal = real;
        real = (tempreal * tempreal) - (imag * imag) + Creal;
        imag = 2.0 * tempreal * imag + Cimag;
        r2   = (real * real) + (imag * imag);
      }

      // Base the color on the number of iterations
      vec3 color;

      if (r2 < 4.0)
      {
        color = InnerColor;
      }else{
        color = mix(OuterColor1, OuterColor2, fract(iter * RangeColor ));
      }

      gl_FragColor = vec4(color, 1.0);
    }
    """

  override def drawBuffers(model: Model3d, texture: Int) {
    val mvpMatrix = makeMVPMatrix
    val vertexBuffer = model.positionsBuffer

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

    GLES20.glVertexAttribPointer(mPositionHandle, COORD_SIZE, GLES20.GL_FLOAT, false, 0, 0)
    checkGlError("glVertexAttribPointer")


    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    checkGlError("glBindBuffer")

    // Draw
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, model.numVertex)
    checkGlError("glDrawArrays")

    // Disable attributes
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    checkGlError("glDisableVertexAttribArray")
  }
}
