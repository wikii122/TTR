package pl.enves.ttr.utils

import spray.json._

object JsonProtocol extends DefaultJsonProtocol {
  /**
   * Default JSON formatter. Converts the data as-is.
   * Throws for unsupported types!
   */
  implicit object ValueFormatter extends JsonFormat[Any] {
    def write(obj: Any) = obj match {
      case d: Double => JsNumber(d)
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b => JsTrue
      case b: Boolean => JsFalse
      case o => throw new IllegalArgumentException(s"Unsupported value during JSON serialization: $o of type ${o.getClass}")
    }

    def read(json: JsValue) = json match {
      case JsNumber(n) if n.isValidInt => n.intValue()
      case JsNumber(n) => n.doubleValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case o => throw new IllegalArgumentException(s"Unsupported value during JSON serialization: $o of type ${o.getClass}")
    }
  }
}
