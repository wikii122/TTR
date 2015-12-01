package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class ReplayAIGame(_human: Player.Value, board: Board = Board()) extends AIGame(board) with Logging {

  private var replayMove = 0

  override def isReplaying = replayMove < movesLog.size

  setHumanSymbol(_human)

  override protected def onMove(move: Game#Move): Boolean = {
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

  override protected def onStart(player: Player.Value): Unit = {
    _player = player
  }

  override def toMap = {
    Map(
      "player" -> _player,
      "human" -> human,
      "replayMove" -> replayMove,
      "board" -> board.toJson,
      "log" -> (movesLog.toList map { entry => entry.toJson }),
      "type" -> Game.REPLAY_AI
    )
  }
}

object ReplayAIGame {
  def apply(game: AIGame): ReplayAIGame = {
    val human = game.getHuman
    val winner = game.winner
    val replayGame = new ReplayAIGame(human.get)
    replayGame.movesLog.appendAll(game.getMovesLog)
    replayGame.board.setWinner(winner)
    return replayGame
  }

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val human = fields("human").convertTo[Option[Player.Value]]
    val replayGame = new ReplayAIGame(human.get, board)
    replayGame._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => replayGame.movesLog.append(LogEntry(jsValue.asJsObject, replayGame)))
    replayGame.replayMove = fields("replayMove").convertTo[Int]
    return replayGame
  }
}
