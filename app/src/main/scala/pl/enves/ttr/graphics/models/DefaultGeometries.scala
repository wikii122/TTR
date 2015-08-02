package pl.enves.ttr.graphics.models

import pl.enves.ttr.graphics.{GeometryData, GeometryProvider}

object DefaultGeometryId extends Enumeration {
  type ModelId = Value
  val Square = Value
}

class DefaultGeometries extends GeometryProvider {

  override def getGeometry: Map[String, GeometryData] = Map(
    (DefaultGeometryId.Square.toString, Square.squareGeometry)
  )
}
