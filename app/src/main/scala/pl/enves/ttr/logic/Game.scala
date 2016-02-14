package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.JsonMappable
import spray.json._
import pl.enves.ttr.utils.JsonProtocol._

import scala.collection.mutable.ListBuffer

/**
 * The Game instance is responsible for handling players and managing board.
 *
 * It is not aware of any of the game rules by itself, as this is
 * the what Board is responsible for.
 */
abstract class Game(protected val board: Board) extends JsonMappable with Logging{
  val gameType: Game.Value

  type State = Seq[Seq[Option[Player.Value]]]

  protected var _player: Player.Value = Player.X

  /**
   * log all successful moves for replay
   */
  protected val movesLog = ListBuffer[LogEntry]()

  def getMovesLog: ListBuffer[LogEntry] = {
    log("movesLog.size: "+movesLog.size)
    return movesLog
  }

  def canBeSaved = true

  def player = _player

  /**
   * Set starting player.
   */
  final def start(startingPlayer: Player.Value) = onStart(startingPlayer)

  /**
   * Make a move, obviously.
   */
  final def make(move: Move): Boolean = {
    if (locked) throw new BoardLocked
    onMove(move)
  }

  def winner: Option[Player.Value] = board.winner

  def finished = board.finished
  final def nonFinished = !finished

  def finishingMove = board.finishingMove

  /**
   * Get board visualization.
   */
  def state: State = board.lines

  /**
   * Get field values
   */
  def quadrantField(quadrant: Quadrant.Value, x: Int, y: Int): Option[Player.Value] = board.quadrantField(quadrant, x, y)

  /**
   * Get quadrant rotation
   */
  def quadrantRotation(quadrant: Quadrant.Value): QRotation.Value = board.quadrantRotation(quadrant)

  /**
   * Get list of available rotations
   */
  def availableRotations: List[Quadrant.Value] = if (nonFinished) board.availableRotations.toList
    else Nil

  /**
   * Can quadrant be rotated
   */
  def canRotate(quadrant: Quadrant.Value) = board.canRotate(quadrant)

  /**
   * Indicates whether this device can alter the board at the moment,
   */
  def locked: Boolean

  protected def onStart(player: Player.Value)

  protected def onMove(move: Move): Boolean

  protected def boardVersion: Int

  override def toMap = Map(
    "player" -> _player,
    "board" -> board.toJson,
    "log" -> (movesLog.toList map { entry => entry.toJson}),
    "type" -> gameType
  )
}

object Game extends Enumeration {
  val STANDARD, BOT, GPS_MULTIPLAYER, CONTINUE, REPLAY = Value

  def create(typo: Game.Value): Game = typo match {
    case STANDARD => StandardGame()
    case BOT => BotGame()
  }

  def load(jsValue: JsValue): Game = {
    jsValue.asJsObject.fields("type").convertTo[Game.Value] match {
      case STANDARD => StandardGame(jsValue)
      case BOT => BotGame(jsValue)
    }
  }
}