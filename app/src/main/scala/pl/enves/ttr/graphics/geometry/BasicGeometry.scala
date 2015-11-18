package pl.enves.ttr.graphics.geometry

import pl.enves.ttr.graphics.AbstractGeometry

case class BasicGeometry(numVertices: Int, drawMode: Int, buffers: Buffers[Int])
  extends AbstractGeometry(numVertices, drawMode, buffers) {
}
