package pl.enves.ttr.utils

import pl.enves.ttr.logic.{Quadrant, Player}
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

  // Temporary workaround, need to be made generic
  implicit object OptionValueFormatter extends JsonFormat[Option[Player.Value]] {
    def write(obj: Option[Player.Value]) = obj match {
      case Some(x) => x.toJson
      case None => JsNull
    }

    def read(json: JsValue) = json match {
      case JsNull => None
      case a => Some(a.convertTo[Player.Value])
    }
  }

  implicit object QuadrantValueFormatter extends JsonFormat[Quadrant.Value] {
    def write(obj: Quadrant.Value) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsString(str) => Quadrant.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit object AnyMapFormatter extends JsonFormat[Map[String, Any]] {
    def write(obj: Map[String, Any]) = JsObject(obj.mapValues(data2json))

    @deprecated("Converting json to Map of any is not fully supported", "07-07-2015")
    def read(json: JsValue) = read2(json)

    def read2(json: JsValue) = json match {
      case j: JsObject => j.fields.mapValues(json2data)
      case o => throw new DeserializationException(s"Unparsable type: ${o.getClass} in instance ${o.toString()}")
    }
  }

  def data2json(any: Any): JsValue = any match {
    case j: JsValue => j
    case d: Double => JsNumber(d)
    case n: Int => JsNumber(n)
    case s: String => JsString(s)
    case b: Boolean if b => JsTrue
    case b: Boolean => JsFalse
    // FIXME Potentially errorprone
    case m: Map[_, _] => m.asInstanceOf[Map[String, Any]].toJson
    case l: List[_] => l.asInstanceOf[List[Any]] map data2json toJson
    case p: Player.Value => p.toJson
    case Some(x) => data2json(x)
    case None => JsNull
    case o => throw new DeserializationException(s"Unparsable type: ${o.getClass} in instance $o")
  }

  def json2data(js: JsValue): Any = js match {
    case JsNumber(n) if n.isValidInt => n.intValue()
    case JsNumber(n) => n.doubleValue()
    case JsString(s) => s
    case JsTrue => true
    case JsFalse => false
    case o => throw new DeserializationException(s"Unparsable type: ${o.getClass} in instance $o)")
  }
}
