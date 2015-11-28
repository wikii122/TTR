package pl.enves.ttr.logic

import java.security.InvalidParameterException

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.ai._
import pl.enves.ttr.logic.inner._
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * AI
 */
class AIGame(human: Player.Value, board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.AI

  private var ai: Option[MinMax] = None

  //TODO: Make interface for different AI classes
  def startThinking(): Unit = {
    implicit val player = if (human == Player.X) Player.O else Player.X

    def makeAIMove(lm: LightMove): Unit = {
      log(s"AI Move: $lm for $player")
      val move = LightMove.toNormalMove(lm, this)
      onMove(move)
    }

    ai = Some(new MinMax(board, player, 2000, 4, makeAIMove))
  }

  /**
   * Initiates the game with given player.
   */
  protected def onStart(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: ${_player}")
    _player = startingPlayer

    if (_player != human) {
      startThinking()
    }
  }

  /**
   * Makes a human move, whether it's a rotation or putting symbol.
   * After it switches to ai.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  protected def onMove(move: Game#Move): Boolean = this.synchronized {
    implicit val player = _player
    if (!move.valid) throw new InvalidParameterException("Given move has expired!")
    if (finished) throw new GameWon(s"Game is finished. $winner has won.")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board move(x, y)
      case Rotation(b, r) => board rotate(b, r)
    }

    movesLog.append(LogEntry(player, move))

    switchPlayer()

    return res
  }

  private def switchPlayer(): Unit = {
    _player = if (player == Player.X) Player.O else Player.X
    log(s"Player set to ${_player}")
    if (_player == human) {
      ai = None
    } else {
      if (!board.finished) {
        startThinking()
      }
    }
  }

  def locked: Boolean = player != human

  protected def boardVersion = board.version

  override def toMap = this.synchronized {
    if (ai.isDefined) {
      ai.get.stop()
    }

    Map(
      "player" -> _player,
      "human" -> human,
      "board" -> board.toJson,
      "log" -> (movesLog.toList map { entry => entry.toJson }),
      "type" -> gameType
    )
  }

  def getHuman: Player.Value = human
}

object AIGame {
  def apply(human: Player.Value) = new AIGame(human)

  def apply(human: Player.Value, board: Board) = new AIGame(human, board)

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val human = fields("human").convertTo[Player.Value]
    val game = new AIGame(human, board)
    game._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => game.movesLog.append(LogEntry(jsValue.asJsObject, game)))

    if (game._player != human) {
      game.startThinking()
    }

    return game
  }
}