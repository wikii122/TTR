package pl.enves.ttr.graphics

import android.opengl.GLES20
import pl.enves.ttr.graphics.models.{Board3x3, Square}


object DefaultGeometryId extends Enumeration {
  type ModelId = Value
  val Rectangle, Board3x3 = Value
}

class DefaultGeometries extends GeometryProvider {
  val squareData = new BuffersData(
    Some(Square.coords),
    None,
    None,
    Some(Square.texCoords)
  )

  val squareGeometry = new ArraysGeometryData(
    Square.numVertex,
    GLES20.GL_TRIANGLE_STRIP,
    squareData
  )

  val board3x3Buffers = new BuffersData(
    Some(Board3x3.coords),
    Some(Board3x3.colors),
    None,
    None
  )

  val board3x3Geometry = new ElementsGeometryData(
    Board3x3.indices.length,
    Board3x3.indices,
    GLES20.GL_LINES,
    board3x3Buffers
  )

  override def getGeometry: Map[String, GeometryData] = Map(
    (DefaultGeometryId.Rectangle.toString, squareGeometry),
    (DefaultGeometryId.Board3x3.toString, board3x3Geometry)
  )
}
