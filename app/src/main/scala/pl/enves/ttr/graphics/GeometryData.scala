package pl.enves.ttr.graphics

case class BuffersData(positions: Option[Array[Float]],
                       texCoords: Option[Array[Float]])

abstract class GeometryData(drawMode: Int, buffers: BuffersData) {
  def getBuffers: BuffersData = buffers
}

case class ArraysGeometryData(numVertices: Int,
                              drawMode: Int,
                              buffers: BuffersData)
  extends GeometryData(drawMode, buffers)

case class ElementsGeometryData(numIndices: Int,
                                indices: Array[Short],
                                drawMode: Int,
                                buffers: BuffersData)
  extends GeometryData(drawMode, buffers)
