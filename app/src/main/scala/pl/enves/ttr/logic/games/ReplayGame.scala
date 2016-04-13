package pl.enves.ttr.logic.games

import pl.enves.androidx.Logging
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

import scala.annotation.tailrec
import scala.util.Try

class ReplayGame(replayedGameType: Game.Value,
                 win: Option[Player.Value],
                 board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.REPLAY

  override def isSavable = false

  override protected def version = 0

  private var replayMove = 0

  private var thread: Option[Thread] = None

  private def makeThread = new Thread(
    new Runnable {
      override def run(): Unit = replayMoves(movesLog.reverse, sleep = true)
    }
  )

  def isReplaying = replayMove < movesLog.length

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

  private def rewind(): Unit = replayMoves(movesLog.reverse.drop(board.version), sleep = false)

  override protected def onMove(move: Move): Boolean = {
    throw new GameWon(s"Game is finished. $winner has won.")
  }

  private[this] def replayMoves(moves: List[LogEntry], sleep: Boolean=false): Unit = moves match {
    case Nil =>
    case entry::rest =>
      log("replaying next move")
      if (sleep) Try{
        Thread.sleep(1000)
      } recoverWith {
        case e: InterruptedException => return
        case e => throw e
      }

      Try {
        entry.move match {
          case Position(x, y) => board.move(x, y)(entry.player)
          case Rotation(b, r) => board.rotate(b, r)(entry.player)
        }
      }

      _player = if (player == Player.X) Player.O else Player.X
      replayMoves(rest, sleep)
  }

  override def locked: Boolean = true

  override def winner: Option[Player.Value] = win

  override protected def start(player: Player.Value): Unit = {
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

    replayGame.movesLog = fields("log").asInstanceOf[JsArray].elements map { any =>
      LogEntry(any.asJsObject)
    } toList

    if (showEnd) replayGame.rewind()
    return replayGame
  }
}
