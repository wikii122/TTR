package pl.enves.ttr.logic.games

import pl.enves.ttr.logic._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * Wrapper for game logic.
 */
class StandardGame(board: Board = Board()) extends Game(board) {
  override val gameType = Game.STANDARD

  /**
   * Initiates the game with given player.
   */
  protected def onStart(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: ${_player}")
    _player = startingPlayer
  }

  /**
   * Makes a move, whether it's a rotation or putting symbol.
   * After it switches to next player.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  protected def onMove(move: Move): Boolean = {
    implicit val player = this.player
    if (finished) throw new GameWon(s"Game is finished. $winner has won.")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board move (x, y)
      case Rotation(b, r) => board rotate (b, r)
    }

    movesLog.append(LogEntry(player, move))

    _player = if (player == Player.X) Player.O else Player.X
    log(s"Player set to ${_player}")

    return res
  }

  def locked: Boolean = false

  protected def boardVersion = board.version
}

object StandardGame {
  def apply() = new StandardGame()

  def apply(board: Board) = new StandardGame(board)

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val game = new StandardGame(board)
    game._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => game.movesLog.append(LogEntry(jsValue.asJsObject)))

    return game
  }
}