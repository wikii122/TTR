package pl.enves.ttr.logic

import java.security.InvalidParameterException

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.ai._
import pl.enves.ttr.logic.inner._
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * AI
 * Plays Os
 * TODO: Shutdown and resume
 */
class AIGame private(board: Board = Board()) extends Game(board) with Logging {
  override protected val gameType = Game.AI

  private var human = Player.X
  private var _locked = false

  private var ai: Option[MinMax] = None

  def makeAIMove(move: LightMove): Unit = {
    implicit val player = Player.O
    log(s"Move: $move for $player")
    // Make a move
    move match {
      case LightPosition(q, x, y) => board move(Quadrant(q), x, y)
      case LightRotation(q, r) => board rotate(Quadrant(q), r)
    }
    switchPlayer()
  }

  //TODO: Make interface for different AI classes
  def startThinking(): Unit = {
    ai = Some(new MinMax(board, makeAIMove))
  }

  /**
   * Initiates the game with given player.
   */
  protected def onStart(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: ${_player}")
    human = startingPlayer
    _player = startingPlayer
  }

  /**
   * Makes a human move, whether it's a rotation or putting symbol.
   * After it switches to ai.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  protected def onMove(move: Move): Boolean = {
    implicit val player = human
    if (!move.valid) throw new InvalidParameterException("Given move has expired!")
    if (finished) throw new GameWon(s"Game is finished. $winner has won.")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board move(x, y)
      case Rotation(b, r) => board rotate(b, r)
    }

    switchPlayer()

    return res
  }

  def switchPlayer(): Unit = {
    _player = if (player == Player.X) Player.O else Player.X
    log(s"Player set to ${_player}")
    if (_player == human) {
      _locked = false
      ai = None
    } else {
      _locked = true
      if (!board.finished) {
        startThinking()
      }
    }
  }

  def locked: Boolean = _locked

  protected def boardVersion = board.version

  override final def toMap = Map(
    "player" -> _player,
    "human" -> human,
    "board" -> board.toJson,
    "type" -> gameType
  )
}

object AIGame {
  def apply() = new AIGame()

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val game = new AIGame(board)
    game._player = fields("player").convertTo[Player.Value]
    game.human = fields("human").convertTo[Player.Value]

    return game
  }
}