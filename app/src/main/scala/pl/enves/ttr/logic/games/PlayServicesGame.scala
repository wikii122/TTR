package pl.enves.ttr.logic.games

import android.app.Activity
import android.content.Intent
import com.google.android.gms.games.Games
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch
import com.google.android.gms.games.multiplayer.{Invitation, Multiplayer}
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.GameActivity
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.Code
import pl.enves.ttr.utils.ExecutorContext._

import scala.util.{Failure, Success}

class PlayServicesGame(board: Board = Board()) extends Game(board) {
  override val gameType: Game.Value = Game.GPS_MULTIPLAYER

  private[this] lazy val activity = ContextRegistry.context.asInstanceOf[GameActivity] // Potentially errorprone
  private[this] var turnBasedMatch: Option[TurnBasedMatch] = None

  override def locked: Boolean = turnBasedMatch.isDefined &&

  override protected def boardVersion: Int = ???

  override protected def onStart(player: Player.Value): Unit = ???

  override protected def onMove(move: Move): Boolean = ???

  private[this] def start(turnBasedMatch: TurnBasedMatch) = ???

  override def onActivityResult(request: Int, response: Int, data: Intent) = request match {
    case Code.SELECT_PLAYERS => if (response == Activity.RESULT_OK) {
      log("Inviting player to match")
      val players = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)
      PlayServices createMatch players onComplete {
        case Success(newMatch) => start(newMatch)
        case Failure(any) => error(s"Failture when creating match with $any")
      }
    } else {
      log("Choose player activity cancelled by player")
      activity.finish()
    }

    case Code.SELECT_INVITATIONS => if (response == Activity.RESULT_OK) {
      val turnBasedMatch: Option[TurnBasedMatch] = Option(data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH))
      val invitation: Option[Invitation] = Option(data.getParcelableExtra(Multiplayer.EXTRA_INVITATION))
      if (turnBasedMatch.isDefined) {
        log("Starting received match")
        start(turnBasedMatch.get)
      } else if (invitation.isDefined) {
        PlayServices accept invitation.get onComplete {
          case Success(newMatch) => start(newMatch)
          case Failure(any) => error(s"Failture when accepting invitation with $any")
        }
      }
    } else {
      log("Select game dialog cancelled")
      activity.finish()
    }
    case a => error(s"onActivityResult did not match request with id: $a")
  }
}

object PlayServicesGame {
  def apply() = new PlayServicesGame(Board())
}