package pl.enves.ttr.logic.games

import com.google.android.gms.games.multiplayer.turnbased.{OnTurnBasedMatchUpdateReceivedListener, TurnBasedMatch}
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.GameActivity
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

import scala.collection.JavaConversions._

class PlayServicesGame(board: Board = Board()) extends Game(board)
with OnTurnBasedMatchUpdateReceivedListener {
  override val gameType: Game.Value = Game.GPS_MULTIPLAYER

  private[this] lazy val myParticipantId: String =
    turnBasedMatch.get getParticipantId PlayServices.playerData.getPlayerId
  private[this] lazy val otherParticipantId: String =
    turnBasedMatch.get.getParticipantIds.toList.filterNot(_ == myParticipantId).head
  private[this] var _turnBasedMatch: Option[TurnBasedMatch] = None
  private[this] lazy val activity: GameActivity = ContextRegistry.context.asInstanceOf[GameActivity]

  private[this] def turnBasedMatch = _turnBasedMatch
  private[this] def turnBasedMatch_=(newMatch: Option[TurnBasedMatch]) = {
    _turnBasedMatch = newMatch
    moved = false
  }
  /** Setting their_turn flag by GPS seems to be too slow, this is fallback indicator. */
  private[this] var moved = false

  override def locked: Boolean = !myTurn

  override def isSavable = false

  override protected def start(player: Player.Value): Unit = {
    log(s"Initializing this player to $player")

    player match {
      case Player.O =>
        _player = player.other
        takeTurn()
      case Player.X =>
        _player = player
    }
  }

  override def resume() = {
    log("Registered match update listener")
    PlayServices.register(this)
  }

  override def pause() = {
    log("Unregistered match update listener")
    PlayServices.unregister()
    
    if (finished)
      PlayServices.finish(turnBasedMatch.get)
  }

  override protected def onMove(move: Move): Boolean = {
    implicit val player = this.player
    if (finished) throw new GameWon(s"Game is finished. $winner has won.")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board move (x, y)
      case Rotation(b, r) => board rotate (b, r)
    }

    movesLog.append(LogEntry(player, move))

    _player = player.other

    if (myTurn)
      takeTurn()
    moved = true

    return res
  }

  def matchId = turnBasedMatch.get.getMatchId

  override def onTurnBasedMatchRemoved(s: String): Unit = ???

  override def onTurnBasedMatchReceived(newMatch: TurnBasedMatch): Unit =
    if (newMatch.getMatchId == turnBasedMatch.get.getMatchId) {
      log("Received update from remote device")
      this.turnBasedMatch = Some(newMatch)

      val data = new String(newMatch.getData, "utf-8")
      updateLocalState(data)
    } else {
      log("Ignored update from different match")
    }

  def start(newMatch: TurnBasedMatch) = {
    log("Received match instance")
    turnBasedMatch = Option(newMatch)
    startMatch(turnBasedMatch.get)
  }

  private[this] def startMatch(turnBasedMatch: TurnBasedMatch) = {
    val data = Option(turnBasedMatch.getData)
    if (data.isDefined) {
      log("Catching up to state")
      updateLocalState(new String(data.get, "utf-8"))
    }
    else initializeMatch()
  }

  private[this] def myTurn =
    turnBasedMatch.isDefined &&
    turnBasedMatch.get.getTurnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN &&
    turnBasedMatch.get.getStatus == TurnBasedMatch.MATCH_STATUS_ACTIVE &&
    !moved

  private[this] def initializeMatch() = {
    log("Sending game initialization data")
    val data = this.toMap.toJson.toString()
    PlayServices.takeTurn(turnBasedMatch.get, data, myParticipantId)
    activity.showChooser(showDifficulty=false)
  }

  private[this] def takeTurn() = {
    PlayServices.takeTurn(turnBasedMatch.get, this.toMap.toJson.toString(), otherParticipantId)
    moved = true
  }

  private[this] def updateLocalState(rawData: String) = {
    val data = rawData.parseJson.asJsObject

    val player = data.fields("player").convertTo[Player.Value]
    _player = if (myTurn) player
      else player.other

    board sync Board(data.fields("board"))
  }
}

object PlayServicesGame {
  def apply() = new PlayServicesGame(Board())
}