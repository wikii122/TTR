package pl.enves.ttr.logic

import pl.enves.ttr.logic.inner.Board

class PlayServicesGame(board: Board = Board()) extends Game(board) {
  override val gameType: Game.Value = Game.GPS_MULTIPLAYER

  override def locked: Boolean = ???

  override protected def boardVersion: Int = ???

  override protected def onStart(player: Player.Value): Unit = ???

  override protected def onMove(move: Game#Move): Boolean = ???
}
