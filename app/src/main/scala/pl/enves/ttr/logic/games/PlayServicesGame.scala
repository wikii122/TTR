package pl.enves.ttr.logic.games

import java.util

import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult
import com.google.android.gms.games.multiplayer.turnbased.{TurnBasedMultiplayer, TurnBasedMatch}
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class PlayServicesGame(inputPlayers: Option[util.ArrayList[String]], board: Board = Board())
  extends Game(board)
  with ResultCallback[TurnBasedMultiplayer.InitiateMatchResult] {
  override val gameType: Game.Value = Game.GPS_MULTIPLAYER
  private[this] var playerSide = Player.X
  private[this] var step = 0
  private[this] val players = inputPlayers getOrElse ???
  private[this] var game: Option[TurnBasedMatch] = None

  PlayServices createMatch (this, players)

  override def locked: Boolean = player != playerSide && step > 0

  override protected def boardVersion: Int = ???

  def initialize() = {
    val data = Map[String, Any](
        "state" -> this.toMap,
        "step" -> step,
        "currentPlayer" -> Player.X.toString // X always starts
      ).toJson.toString()

    PlayServices.takeTurn(game.get, data)
  }

  def update(data: String) = {
    val state = data.parseJson
  }


  override def onResult(r: InitiateMatchResult): Unit = {
    game = Option(r.getMatch)
    if (game.isDefined) {
      Option(game.get.getData) match {
        case None => initialize()
        case Some(state) => update(state.toString)
      }
    } else {
      error(s"Game offline, reason: ${r.getStatus}")
    }
  }

  override protected def onMove(move: Move): Boolean = {
    implicit val player = this.player

    if (finished) throw new GameWon(s"Game is finished. $winner has won.")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board move (x, y)
      case Rotation(b, r) => board rotate (b, r)
    }

    _player = player.other
    step += 1

    log(s"Player set to ${_player}")

    ???

    return res
  }

  override protected def onStart(player: Player.Value): Unit = {
    playerSide = player
  }
}

object PlayServicesGame {
  def apply(players: Option[util.ArrayList[String]]) = new PlayServicesGame(players)
}