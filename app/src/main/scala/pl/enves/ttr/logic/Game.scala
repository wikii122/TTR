package pl.enves.ttr.logic

import java.util

import android.content.Intent
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch
import pl.enves.androidx.Logging
import pl.enves.ttr.logic.games._
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.networking.{Achievement, PlayServices}
import pl.enves.ttr.utils.JsonMappable
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

import scala.collection.mutable.ListBuffer

/**
 * The Game instance is responsible for handling players and managing board.
 *
 * It is not aware of any of the game rules by itself, as this is
 * the what Board is responsible for.
 */
abstract class Game(protected val board: Board) extends JsonMappable with Logging {
  val gameType: Game.Value

  type State = Seq[Seq[Option[Player.Value]]]

  protected var _player: Player.Value = Player.X

  var movesLog: List[LogEntry] = Nil

  def isSavable = true

  def player = _player

  /**
   * Set starting player.
   */
  def playerSide = player
  final def playerSide_=(startingPlayer: Player.Value) = start(startingPlayer)

  def pause() = {}

  def resume() = {}

  /**
   * Stop game processes
   */
  final def stop(): Unit = onStop()

  /**
   * Make a move, obviously.
   */
  final def make(move: Move): Boolean = {
    if (locked) throw new BoardLocked
    val res = onMove(move)

    if (version == 1)
      PlayServices.achievement.step(Achievement.achievement100thGame)

    if (res)
      PlayServices.achievement.step(Achievement.achievement50thWin)

    return res
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
  def fieldState(x: Int, y: Int): Option[Player.Value] = board.fieldState(x, y)

  /**
   * Get quadrant rotation
   */
  def quadrantRotation(quadrant: Quadrant.Value): QRotation.Value = board.quadrantRotation(quadrant)

  /**
   * Get list of available rotations
   */
  def availableRotations: List[Quadrant.Value] =
    if (nonFinished) board.availableRotations.toList
    else Nil

  /**
   * Can quadrant be rotated
   */
  def canRotate(quadrant: Quadrant.Value) = board.canRotate(quadrant)

  /**
   * Indicates whether this device can alter the board at the moment,
   */
  def locked: Boolean

  protected def start(player: Player.Value)

  protected def onStop(): Unit = {}

  protected def onMove(move: Move): Boolean
  
  protected def version: Int = board.version

  override def toMap = Map(
    "player" -> _player,
    "board" -> board.toJson,
    "log" -> (movesLog map {_.toJson}),
    "type" -> gameType
  )
}

object Game extends Enumeration {
  val STANDARD, BOT, GPS_MULTIPLAYER, CONTINUE, REPLAY = Value

  def plain() = StandardGame()

  def network() = PlayServicesGame()

  def bot() = BotGame()

  def load(jsValue: JsValue): Game = {
    jsValue.asJsObject.fields("type").convertTo[Game.Value] match {
      case STANDARD => StandardGame(jsValue)
      case BOT => BotGame(jsValue)
    }
  }
}