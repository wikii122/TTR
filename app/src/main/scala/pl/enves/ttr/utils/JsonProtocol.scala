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
}
