package pl.enves.ttr.graphics

case class BuffersData(positions: Option[Array[Float]],
                       colors: Option[Array[Float]],
                       normals: Option[Array[Float]],
                       texCoords: Option[Array[Float]])

abstract class GeometryData(drawMode: Int, buffers:BuffersData) {
  def getBuffers: BuffersData = {
    return buffers
  }
}

case class ArraysGeometryData(numVertices: Int,
                              drawMode: Int,
                              buffers: BuffersData)
  extends GeometryData(drawMode, buffers) {
}

case class ElementsGeometryData(numIndices: Int,
                                indices: Array[Short],
                                drawMode: Int,
                                buffers: BuffersData)
  extends GeometryData(drawMode, buffers) {
}