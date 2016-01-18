package pl.enves.ttr.graphics.geometry

import java.nio.{ByteBuffer, ByteOrder, IntBuffer}

import android.opengl.GLES20

trait Geometry {
  def getNumVertices: Int

  // one of POINTS, LINE_STRIP, LINE_LOOP, LINES, TRIANGLE_STRIP, TRIANGLE_FAN, TRIANGLES
  def getDrawMode: Int

  def getBuffers: Buffers[Int]

  final val PositionSize = 3
  final val TexCoordSize = 2

  def draw(): Unit = {
    GLES20.glDrawArrays(getDrawMode, 0, getNumVertices)
  }

  // Buffer in GPU memory
  protected def createFloatBuffer(arr: Array[Float]): Int = {
    val floatBuffer = ByteBuffer.allocateDirect(arr.length * 4)
      .order(ByteOrder.nativeOrder()).asFloatBuffer()

    floatBuffer.put(arr).position(0)

    val name = IntBuffer.allocate(1)
    GLES20.glGenBuffers(1, name)
    val buffer = name.get(0)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer)

    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arr.length * 4, floatBuffer, GLES20.GL_STATIC_DRAW)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    return buffer
  }

  protected def unflipY(arr: Array[Float]): Array[Float] = {
    val ret = new Array[Float](arr.length)
    for (i <- arr.indices) {
      if (i % 2 == 0) {
        ret(i) = arr(i)
      } else {
        ret(i) = 1.0f - arr(i)
      }
    }
    return ret
  }
}
