package pl.enves.ttr.utils
/**
 * Indicates that the class can be serialized to JSON.
 */
trait Jsonable {
  def toJson: String = ???

  def loadJson(data: String)

  def toMap: Map[String, Any]
}
