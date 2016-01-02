package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.ai._
import pl.enves.ttr.logic.inner._
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * AI
 */
class AIGame(board: Board = Board()) extends Game(board) with Logging {
  override val gameType = Game.AI

  private var ai: Option[MinMax] = None

  protected var human: Option[Player.Value] = None

  private var maxTime: Int = 3000

  private var maxDepth: Int = 3

  private var adaptiveDepth = true

  private var positionsChoosing = PositionsChoosing.Reasonable

  private var bestMoveHeuristics = BestMoveHeuristics.PreviousIteration

  private var displayStatus: Option[String => Unit] = None

  private var randomizeDecisions = false

  //TODO: remove in production
  private var enabled = true

  //TODO: Make interface for different AI classes
  def startThinking(): Unit = {
    implicit val player = if (human.get == Player.X) Player.O else Player.X

    def makeAIMove(move: Move): Unit = {
      log(s"AI Move: $move for $player")
      onMove(move)
    }

    //TODO: make it more intelligent
    val depth = if (adaptiveDepth) Math.min(36 - board.getFreeFields + 1, maxDepth)
    else maxDepth

    ai = Some(new MinMax(board, player, maxTime, depth, positionsChoosing, bestMoveHeuristics, displayStatus, randomizeDecisions, makeAIMove))
  }

  /**
   * Initiates the game with given player.
   */
  protected def onStart(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: ${_player}")
    _player = startingPlayer

    if (_player != human.get && enabled) {
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
   * After it switches to ai.
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
      ai = None
    } else {
      if (!board.finished && enabled) {
        startThinking()
      }
    }
  }

  def locked: Boolean = if (human.isDefined) {
    player != human.get && enabled
  } else {
    true
  }

  protected def boardVersion = board.version

  override def toMap = this.synchronized {
    if (ai.isDefined) {
      ai.get.stop()
    }

    Map(
      "player" -> _player,
      "human" -> human,
      "maxTime" -> maxTime,
      "maxDepth" -> maxDepth,
      "positionsChoosing" -> positionsChoosing.id,
      "enabled" -> enabled,
      "board" -> board.toJson,
      "log" -> (movesLog.toList map { entry => entry.toJson }),
      "type" -> gameType
    )
  }

  def getHuman: Option[Player.Value] = human

  def setMaxTime(max: Int): Unit = maxTime = max

  def setMaxDepth(max: Int): Unit = maxDepth = max

  def setAdaptiveDepth(a: Boolean): Unit = adaptiveDepth = a

  def setPositionsChoosing(pc: PositionsChoosing.Value): Unit = positionsChoosing = pc

  def setBestMoveHeuristics(bmh: BestMoveHeuristics.Value): Unit = bestMoveHeuristics = bmh

  def setDisplayStatus(f: String => Unit): Unit = displayStatus = Some(f)

  def setRandomizeDecisions(rd: Boolean): Unit = randomizeDecisions = rd

  def setEnabled(e: Boolean): Unit = this.synchronized {
    enabled = e
    if (enabled) {
      if (human.isDefined && _player != human.get && !board.finished) {
        startThinking()
      }
    } else {
      if (ai.isDefined) {
        ai.get.stop()
      }
    }
  }
}

object AIGame {
  def apply(): Game = new AIGame()

  def apply(board: Board): Game = new AIGame(board)

  def apply(jsValue: JsValue): Game = {
    val fields = jsValue.asJsObject.fields
    val board = Board(fields("board"))
    val human = fields("human").convertTo[Option[Player.Value]]
    val game = new AIGame(board)
    game._player = fields("player").convertTo[Player.Value]
    fields("log").asInstanceOf[JsArray].elements foreach (jsValue => game.movesLog.append(LogEntry(jsValue.asJsObject, game)))

    val maxTime = fields("maxTime").convertTo[Int]
    game.setMaxTime(maxTime)

    val maxDepth = fields("maxDepth").convertTo[Int]
    game.setMaxDepth(maxDepth)

    val pc = PositionsChoosing(fields("positionsChoosing").convertTo[Int])
    game.setPositionsChoosing(pc)

    game.enabled = fields("enabled").convertTo[Boolean]

    if (human.isDefined) {
      game.setHumanSymbol(human.get)

      if (game._player != human.get && game.nonFinished && game.enabled) {
        game.startThinking()
      }
    }

    return game
  }
}