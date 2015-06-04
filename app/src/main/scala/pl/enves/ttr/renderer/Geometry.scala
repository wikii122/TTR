package pl.enves.ttr.renderer

import android.opengl.GLES20

/**
 * Class to store Vertex Buffer Objects that belong to the same model
 */
case class VBOs(positions: Int, colors: Int, normals: Int, texCoords: Int)

/**
 * Class that knows to which basic graphic primitives data in VBOs belongs
 * @param drawMode one of POINTS, LINE_STRIP, LINE_LOOP, LINES, TRIANGLE_STRIP, TRIANGLE_FAN, TRIANGLES
 */
abstract class Geometry(drawMode: Int, vbos: VBOs) {

  def getVBOS: VBOs = {
    return vbos
  }

  def draw(): Unit
}

/**
 * Vertices are already stored in the right order
 * @param numVertices number of vertices to draw
 */
class GeometryArrays(numVertices: Int, drawMode: Int, vbos: VBOs)
  extends Geometry(drawMode, vbos) {
  override def draw(): Unit = {
    GLES20.glDrawArrays(drawMode, 0, numVertices)
  }
}

/**
 * Order of vertices is non-linear
 * @param numIndices number of vertices to draw (1 index => 1 vertex)
 * @param indices buffer with vertex indices
 */
class GeometryElements(numIndices: Int, indices: Int, drawMode: Int, vbos: VBOs)
  extends Geometry(drawMode, vbos: VBOs) {
  override def draw(): Unit = {
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices)
    GLES20.glDrawElements(drawMode, numIndices, GLES20.GL_UNSIGNED_SHORT, 0)
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
  }
}
