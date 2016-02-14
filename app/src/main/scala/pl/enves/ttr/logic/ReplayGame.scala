package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

class ReplayGame(replayedGameType: Game.Value,
                 win: Option[Player.Value],
                 board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.REPLAY

  override def canBeSaved = false

  override protected def boardVersion = 0

  private var replayMove = 0

  private var thread: Option[Thread] = None

  private def makeThread = new Thread(new Runnable {
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

  def startReplaying() = {
    stopReplaying()

    thread = Some(makeThread)
    thread.get.start()
  }

  def stopReplaying() = {
    if (thread.isDefined) {
      if (thread.get.isAlive) {
        thread.get.interrupt()
        thread.get.join()
      }
      thread = None
    }
  }

  def rewind(): Unit = {
    var done = false
    while (!done) {
      done = !replayNextMove()
    }
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

  override def locked: Boolean = true

  override def winner: Option[Player.Value] = win

  override protected def onStart(player: Player.Value): Unit = {
    _player = player
  }

  override def toMap = throw new Exception("Replay game shouldn't be saved")
}

object ReplayGame {

  def apply(jsValue: JsValue, showEnd: Boolean): Game = {
    val fields = jsValue.asJsObject.fields
    val gameType = fields("type").convertTo[Game.Value]
    val board = Board(fields("board"))
    val replayGame = new ReplayGame(gameType, board.winner)
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => replayGame.movesLog.append(LogEntry(jsValue.asJsObject)))
    if (showEnd) replayGame.rewind()
    return replayGame
  }
}
