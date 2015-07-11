package pl.enves.ttr.graphics

/**
 *
 */
trait GeometryProvider {
  def getGeometry: Map[String, GeometryData]
}
