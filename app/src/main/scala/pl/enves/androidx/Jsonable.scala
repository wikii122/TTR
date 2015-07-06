package pl.enves.androidx

/**
 * Indicates that the class can be serialized to JSON.
 */
trait Jsonable {
  def dump: String

  def load(data: String)
}
