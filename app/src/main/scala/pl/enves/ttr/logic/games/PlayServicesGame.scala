package pl.enves.ttr.logic.games

import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.GameActivity
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board

class PlayServicesGame(board: Board = Board()) extends Game(board) {
  override val gameType: Game.Value = Game.GPS_MULTIPLAYER

  private[this] lazy val activity = ContextRegistry.context.asInstanceOf[GameActivity] // Potentially errorprone

  private[this] var turnBasedMatch: Option[TurnBasedMatch] = None

  override def locked: Boolean = !myTurn

  override protected def onStart(player: Player.Value): Unit = ???

  override protected def onMove(move: Move): Boolean = ???

  def start(turnBasedMatch: TurnBasedMatch) = ???

  private[this] def startMatch(turnBasedMatch: TurnBasedMatch) = ???

  private[this] def myTurn =
    turnBasedMatch.isDefined &&
    turnBasedMatch.get.getTurnStatus == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN
}

object PlayServicesGame {
  def apply() = new PlayServicesGame(Board())
}