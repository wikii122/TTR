package pl.enves.ttr.logic.games

import pl.enves.androidx.Logging
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import spray.json._

import scala.util.Try

class ReplayGame(movesToReplay: List[LogEntry],
                 win: Option[Player.Value],
                 board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.REPLAY

  override def isSavable = false

  override protected def version = 0

  private[this] var thread: Option[Thread] = None

  private[this] def makeThread = new Thread(
    new Runnable {
      override def run(): Unit = replayMoves(movesToReplay)
    }
  )

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

  override protected def onMove(move: Move): Boolean = {
    throw new GameWon(s"Game is finished. $winner has won.")
  }

  private[this] def replayMoves(moves: List[LogEntry]): Unit = moves match {
    case Nil =>
    case entry :: rest =>
      log(s"Replaying moves, left ${rest.length}")
      try {
        Thread.sleep(1000)
      } catch {
        case e: InterruptedException => return
      }

      Try {
        entry.move match {
          case Position(x, y) => board.move(x, y)(entry.player)
          case Rotation(b, r) => board.rotate(b, r)(entry.player)
        }
        movesLog = entry :: movesLog
      }

      _player = if (player == Player.X) Player.O else Player.X
      replayMoves(rest)
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
    val board = Board(fields("board"))

    val movesLog = fields("log").asInstanceOf[JsArray].elements map { any =>
      LogEntry(any.asJsObject)
    } toList

    if (showEnd) {
      val replayGame = new ReplayGame(Nil, board.winner, board)
      if (movesLog.nonEmpty) replayGame._player = movesLog.last.player
      replayGame
    } else {
      new ReplayGame(movesLog.reverse, board.winner)
    }
  }
}
