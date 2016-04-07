package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.utils.JsonMappable
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class LogEntry(val player: Player.Value, val move: Move) extends Logging with JsonMappable {
  
  override def toMap: Map[String, Any] = Map(
    "player" -> player.toJson,
    "move" -> move.toJson
  )
}

object LogEntry {
  def apply(player: Player.Value, move: Move) = new LogEntry(player, move)

  def apply(jsValue: JsValue): LogEntry = {
    val fields = jsValue.asJsObject.fields
    val player = fields("player").convertTo[Player.Value]
    val move = fields("move").convertTo[Move]
    return new LogEntry(player, move)
  }
}
