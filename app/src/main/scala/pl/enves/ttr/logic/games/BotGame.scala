package pl.enves.ttr.logic.games

import pl.enves.androidx.Logging
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.bot._
import pl.enves.ttr.logic.inner._
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * Bot
 */
class BotGame(board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.BOT

  private[this] var algorithm: Option[MinMax] = None

  private[this] var human: Option[Player.Value] = None

  private[this] val minTime: Int = 1000

  private[this] var maxTime: Int = 3000

  private[this] val maxDepth: Int = 5

  private[this] val adaptiveDepth = true

  private[this] val randomizeDecisions = true

  //TODO: Make interface for different algorithm classes
  def startThinking(): Unit = {
    implicit val player = if (human.get == Player.X) Player.O else Player.X

    def makeBotMove(move: Move): Unit = {
      log(s"Bot Move: $move for $player")
      onMove(move)
    }

    //TODO: make it more intelligent
    val depth = if (adaptiveDepth) Math.min(36 - board.getFreeFields + 1, maxDepth)
    else maxDepth

    algorithm = Some(new MinMax(board, player, minTime, maxTime, depth, randomizeDecisions, makeBotMove))
  }

  /**
   * Initiates the game with given player.
   */
  protected def onStart(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: ${_player}")
    _player = startingPlayer

    if (_player != human.get) {
      startThinking()
    }
  }

  def setHumanSymbol(symbol: Player.Value): Unit = {
    if (human.isEmpty) {
      human = Some(symbol)

    } else {
      throw new UnsupportedOperationException("Cannot redefine human symbol")
    }
  }

  override def canBeSaved: Boolean = human.isDefined

  /**
   * Makes a human move, whether it's a rotation or putting symbol.
   * After it switches to bot.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  protected def onMove(move: Move): Boolean = this.synchronized {
    implicit val player = _player
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
    if (_player == human.get) {
      algorithm = None
    } else {
      if (!board.finished) {
        startThinking()
      }
    }
  }

  def locked: Boolean = if (human.isDefined) {
    player != human.get
  } else {
    true
  }

  protected def boardVersion = board.version

  override def toMap = this.synchronized {
    if (algorithm.isDefined) {
      algorithm.get.stop()
    }

    Map(
      "player" -> _player,
      "human" -> human,
      "maxTime" -> maxTime,
      "board" -> board.toJson,
      "log" -> (movesLog.toList map { entry => entry.toJson }),
      "type" -> gameType
    )
  }

  def getHuman: Option[Player.Value] = human

  def setMaxTime(max: Int): Unit = maxTime = max
}

object BotGame {
  def apply(): Game = new BotGame()

  def apply(board: Board): Game = new BotGame(board)

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val human = fields("human").convertTo[Option[Player.Value]]
    val game = new BotGame(board)
    game._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => game.movesLog.append(LogEntry(jsValue.asJsObject)))

    val maxTime = fields("maxTime").convertTo[Int]
    game.setMaxTime(maxTime)

    if (human.isDefined) {
      game.setHumanSymbol(human.get)

      if (game._player != human.get && game.nonFinished) {
        game.startThinking()
      }
    }

    return game
  }
}