package pl.enves.ttr.utils

import spray.json._
import JsonProtocol._

trait JsonMappable extends Mappable {
  def toJson: JsValue = toMap.toJson
}
