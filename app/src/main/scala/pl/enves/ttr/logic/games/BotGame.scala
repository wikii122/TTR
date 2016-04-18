package pl.enves.ttr.logic.games

import pl.enves.androidx.Logging
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.bot._
import pl.enves.ttr.logic.inner._
import pl.enves.ttr.logic.networking.{Achievement, PlayServices}
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
  private[this] var _difficulty = 0
  private[this] val adaptiveTime = true
  private[this] val randomizeDecisions = true

  private var maxTime: Int = 3000
  private var maxDepth: Int = 5

  //TODO: Make interface for different algorithm classes
  private def startThinking(): Unit = {
    implicit val player = if (human.get == Player.X) Player.O else Player.X

    def makeBotMove(move: Move): Unit = {
      log(s"Bot Move: $move for $player")
      onMove(move)
    }

    val time = if (adaptiveTime) Math.min((36 - board.getFreeFields + 1) * 1000, maxTime)
    else maxTime

    algorithm = Some(new MinMax(board, player, minTime, time, maxDepth, randomizeDecisions, makeBotMove))
  }

  def startThinkingIfNeeded(): Unit = {
    if (algorithm.isEmpty && human.isDefined && human.get != _player && nonFinished) {
      startThinking()
    }
  }

  /**
   * Initiates the game with given player.
   */
  protected def start(startingPlayer: Player.Value) = {

    log("Creating new game")
    log(s"Starting player: ${_player}")
    _player = startingPlayer

    startThinkingIfNeeded()
  }

  override protected def onStop(): Unit = this.synchronized {
    if (algorithm.isDefined) {
      algorithm.get.stop()
      algorithm = None
    }
  }

  def setHumanSymbol(symbol: Player.Value): Unit = {
    if (human.isEmpty) {
      human = Some(symbol)

    } else {
      throw new UnsupportedOperationException("Cannot redefine human symbol")
    }
  }

  override def isSavable: Boolean = human.isDefined

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

    movesLog = LogEntry(player, move) :: movesLog

    switchPlayer()

    if (difficulty == 9 && res &&
      human.isDefined &&
      player == human.get)
      PlayServices.achievement.unlock(Achievement.achievementWinWithHardestBot)

    return res
  }

  private def switchPlayer(): Unit = {
    _player = if (player == Player.X) Player.O else Player.X
    log(s"Player set to ${_player}")
    if (_player == human.get) {
      algorithm = None
    }
    startThinkingIfNeeded()
  }

  def locked: Boolean = if (human.isDefined) {
    player != human.get
  } else {
    true
  }

  override def toMap = Map(
    "player" -> _player,
    "human" -> human,
    "maxTime" -> maxTime,
    "board" -> board.toJson,
    "log" -> (movesLog map (_.toJson)),
    "type" -> gameType
  )

  def getHuman: Option[Player.Value] = human

  def difficulty = _difficulty
  def difficulty_=(i: Int) = {
    log(s"Setting difficulty to $i")
    _difficulty = i
    maxTime = (difficulty + 1) * 1000
    maxDepth = difficulty + 1
  }
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

    game.movesLog = fields("log").asInstanceOf[JsArray].elements map { any =>
      LogEntry(any.asJsObject)
    } toList

    game.maxTime = fields("maxTime").convertTo[Int]

    if (human.isDefined) {
      game.setHumanSymbol(human.get)
    }

    game.startThinkingIfNeeded()

    return game
  }
}