package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class ReplayGame(replayedGameType: Game.Value,
                 devicePlayer: Option[Player.Value],
                 win: Option[Player.Value],
                 board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.REPLAY

  override protected def boardVersion = 0

  private var replayMove = 0

  private val thread = new Thread(new Runnable {
    override def run(): Unit = {
      var done = false
      while (!done) {
        try {
          Thread.sleep(1000)
        } catch {
          case e: InterruptedException =>
            return
        }
        done = !replayNextMove()
      }
    }
  })

  def isReplaying = replayMove < movesLog.size

  def getReplayedGameType = replayedGameType

  def getDevicePlayer = devicePlayer

  def startReplaying() = {
    if (thread.isAlive) {
      stopReplaying()
    }

    thread.start()
  }

  def stopReplaying() = {
    thread.interrupt()
    thread.join()
  }

  override protected def onMove(move: Move): Boolean = {
    throw new GameWon(s"Game is finished. $winner has won.")
  }

  def replayNextMove(): Boolean = {
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

  override def locked: Boolean = if (devicePlayer.isDefined) _player != devicePlayer.get else true

  override def winner: Option[Player.Value] = win

  override protected def onStart(player: Player.Value): Unit = {
    _player = player
  }

  override def toMap = {
    Map(
      "player" -> _player,
      "devicePlayer" -> devicePlayer,
      "winner" -> win,
      "replayMove" -> replayMove,
      "board" -> board.toJson,
      "log" -> (movesLog.toList map { entry => entry.toJson }),
      "type" -> Game.REPLAY,
      "replayedGameType" -> replayedGameType
    )
  }
}

object ReplayGame {
  def apply(game: StandardGame): ReplayGame = {
    val replayGame = new ReplayGame(Game.STANDARD, None, game.winner)
    replayGame.movesLog.appendAll(game.getMovesLog)
    replayGame.startReplaying()
    return replayGame
  }

  def apply(game: AIGame): ReplayGame = {
    val replayGame = new ReplayGame(Game.AI, game.getHuman, game.winner)
    replayGame.movesLog.appendAll(game.getMovesLog)
    replayGame.startReplaying()
    return replayGame
  }

  //TODO: GPS_MULTIPLAYER

  def apply(game: ReplayGame): ReplayGame = {
    val replayGame = new ReplayGame(game.getReplayedGameType, game.getDevicePlayer, game.winner)
    replayGame.movesLog.appendAll(game.getMovesLog)
    replayGame.startReplaying()
    return replayGame
  }

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val devicePlayer = fields("devicePlayer").convertTo[Option[Player.Value]]
    val winner = fields("winner").convertTo[Option[Player.Value]]
    val replayedGameType = fields("replayedGameType").convertTo[Game.Value]
    val replayGame = new ReplayGame(replayedGameType, devicePlayer, winner, board)
    replayGame._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => replayGame.movesLog.append(LogEntry(jsValue.asJsObject, replayGame)))
    replayGame.replayMove = fields("replayMove").convertTo[Int]
    replayGame.startReplaying()
    return replayGame
  }
}
