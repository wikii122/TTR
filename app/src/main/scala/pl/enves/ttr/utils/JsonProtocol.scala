package pl.enves.ttr.utils

import pl.enves.ttr.logic.Player
import spray.json._

object JsonProtocol extends DefaultJsonProtocol {
  /**
   * Default JSON formatter. Converts the data as-is.
   * Throws for unsupported types!
   */
  implicit object PlayerValueFormatter extends JsonFormat[Player.Value] {
    def write(obj: Player.Value) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsString(str) => Player.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit object AnyMapFormatter extends JsonFormat[Map[String, Any]] {
    def write(obj: Map[String, Any]) = JsObject(obj.mapValues(data2json))

    def read(json: JsValue) = json match {
      case j: JsObject => j.fields.mapValues(json2data)
      case o => throw new DeserializationException(s"Unparsable type: ${o.getClass} in instance ${o.toString()}")
    }


    def data2json(any: Any): JsValue = any match {
      case d: Double => JsNumber(d)
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b => JsTrue
      case b: Boolean => JsFalse
      case m: Map[_, _] => write(m.asInstanceOf[Map[String, Any]])
      case p: Player.Value => p.toJson
      case o => throw new DeserializationException(s"Unparsable type: ${o.getClass} in instance ${o.toString}")
    }

    def json2data(js: JsValue): Any = js match {
      case JsNumber(n) if n.isValidInt => n.intValue()
      case JsNumber(n) => n.doubleValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case j: JsObject => read(j)
      case o => throw new DeserializationException(s"Unparsable type: ${o.getClass} in instance ${o.toString()}")
    }
  }
}
