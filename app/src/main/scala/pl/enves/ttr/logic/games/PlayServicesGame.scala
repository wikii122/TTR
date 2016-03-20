package pl.enves.ttr.logic.games

import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.JsonProtocol._

// Import optimization deletes:
// import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

import scala.collection.JavaConversions._

class PlayServicesGame(board: Board = Board()) extends Game(board) {
  override val gameType: Game.Value = Game.GPS_MULTIPLAYER

  private[this] lazy val currentParticipantId: String =
    turnBasedMatch.get getParticipantId PlayServices.playerData.getPlayerId
  private[this] lazy val otherParticipantId: String =
    turnBasedMatch.get.getParticipantIds.toList.filterNot(_ == currentParticipantId).head

  private[this] var turnBasedMatch: Option[TurnBasedMatch] = None

  override def locked: Boolean = !myTurn

  override protected def onStart(player: Player.Value): Unit = ???

  override protected def onMove(move: Move): Boolean = ???

  def start(newMatch: TurnBasedMatch) = {
    log("Received match instance")
    turnBasedMatch = Option(newMatch)
    startMatch(turnBasedMatch.get)
  }

  private[this] def startMatch(turnBasedMatch: TurnBasedMatch) = {
    val data = Option(turnBasedMatch.getData)
    if (data.isDefined) {
      updateState(new String(data.get, "utf-8"))
    }
    else initializeMatch()
  }

  private[this] def myTurn =
    turnBasedMatch.isDefined &&
    turnBasedMatch.get.getTurnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN

  private[this] def initializeMatch() = {
    log("Sending game initialization data")
    val data = this.toMap.toJson.toString()
    PlayServices.takeTurn(turnBasedMatch.get, data, otherParticipantId)
  }

  private[this] def updateState(rawData: String) = {
    val data = rawData.parseJson.asJsObject

    val player = data.fields("player").convertTo[Player.Value]
    _player = if (myTurn) player
      else player.other


  }
}

object PlayServicesGame {
  def apply() = new PlayServicesGame(Board())
}