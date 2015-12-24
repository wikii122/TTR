package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.utils.JsonMappable
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class LogEntry(player: Player.Value, move: Move) extends Logging with JsonMappable {
  def getPlayer = player

  def getMove = move

  override def toMap: Map[String, Any] = Map(
    "player" -> player.toJson,
    "move" -> TemporaryWorkaround.moveToInt(move) //TODO
  )
}

object LogEntry {
  def apply(player: Player.Value, move: Move) = new LogEntry(player, move)

  def apply(jsValue: JsValue, game: Game): LogEntry = {
    val fields = jsValue.asJsObject.fields
    val player = fields("player").convertTo[Player.Value]
    val move = TemporaryWorkaround.intToMove(fields("move").convertTo[Int], game) //TODO
    return new LogEntry(player, move)
  }
}

//TODO: remove when changes to JsonProtocol from Networking become available
object TemporaryWorkaround {
  def moveToInt(move: Move): Int = move match {
    case p: Position => p.x * 6 + p.y
    case r: Rotation => 36 + r.board.id * 4 + r.r.id
  }

  def intToMove(i: Int, game: Game): Move = if (i < 36) {
    Position(i / 6, i % 6)
  } else {
    val j = i - 36
    Rotation(Quadrant(j / 4), QRotation(j % 4))
  }
}