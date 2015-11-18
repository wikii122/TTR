package pl.enves.ttr.graphics.geometry

import pl.enves.ttr.graphics.AbstractGeometry

case class TextGeometry(numVertices: Int, drawMode: Int, buffers: Buffers[Int], width: Float, height: Float)
  extends AbstractGeometry(numVertices, drawMode, buffers) {
}
