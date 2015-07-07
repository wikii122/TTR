package pl.enves.ttr.graphics

case class ProcBuffers(positions: Option[Array[Float]],
                       colors: Option[Array[Float]],
                       normals: Option[Array[Float]],
                       texCoords: Option[Array[Float]])

abstract class ProcGeometry(drawMode: Int, buffers: ProcBuffers) {
  def getBuffers: ProcBuffers = {
    return buffers
  }
}

case class ProcGeometryArrays(numVertices: Int,
                              drawMode: Int,
                              buffers: ProcBuffers)
  extends ProcGeometry(drawMode, buffers) {
}

case class ProcGeometryElements(numIndices: Int,
                                indices: Array[Short],
                                drawMode: Int,
                                buffers: ProcBuffers)
  extends ProcGeometry(drawMode, buffers: ProcBuffers) {
}