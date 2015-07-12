package pl.enves.ttr.graphics.models

import pl.enves.ttr.graphics.{GeometryData, GeometryProvider}

object DefaultGeometryId extends Enumeration {
  type ModelId = Value
  val Square, Board3x3 = Value
}

class DefaultGeometries extends GeometryProvider {

  override def getGeometry: Map[String, GeometryData] = Map(
    (DefaultGeometryId.Square.toString, Square.squareGeometry),
    (DefaultGeometryId.Board3x3.toString, Board3x3.board3x3Geometry)
  )
}
