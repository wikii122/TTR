package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class ReplayStandardGame(board: Board = Board()) extends StandardGame(board) with Logging {

  private var replayMove = 0

  override def isReplaying = replayMove < movesLog.size

  override protected def onMove(move: Move): Boolean = {
    throw new GameWon(s"Game is finished. $winner has won.")
  }

  override def replayNextMove(): Boolean = {
    log("replaying next move")
    if (replayMove < movesLog.size) {
      val entry = movesLog(replayMove)
      implicit val player = entry.getPlayer
      entry.getMove match {
        case Position(x, y) => board move(x, y)
        case Rotation(b, r) => board rotate(b, r)
      }
      replayMove += 1
      _player = if (player == Player.X) Player.O else Player.X
      return true
    }
    return false
  }

  override def locked: Boolean = true

  override protected def onStart(startingPlayer: Player.Value) = {
    _player = startingPlayer
  }

  override def toMap = Map(
    "player" -> _player,
    "replayMove" -> replayMove,
    "board" -> board.toJson,
    "log" -> (movesLog.toList map { entry => entry.toJson }),
    "type" -> Game.REPLAY_STANDARD
  )
}

object ReplayStandardGame {
  def apply(game: StandardGame): Game = {
    val winner = game.winner
    val replayGame = new ReplayStandardGame()
    replayGame.movesLog.appendAll(game.getMovesLog)
    replayGame.board.setWinner(winner)
    return replayGame
  }

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val replayGame = new ReplayStandardGame(board)
    replayGame._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => replayGame.movesLog.append(LogEntry(jsValue.asJsObject, replayGame)))
    replayGame.replayMove = fields("replayMove").convertTo[Int]
    return replayGame
  }
}
