package pl.enves.ttr.logic

import pl.enves.ttr.logic.inner.Board

/**
 * The Game instance is responsible for handling players and managing board.
 *
 * It is not aware of any of the game rules by itself, as this is
 * the what Board is responsible for.
 */
abstract class Game(protected val board: Board) {
  type State = Seq[Seq[Option[Player.Value]]]

  protected var _player: Player.Value = Player.X

  def player = _player

  /**
   * Set starting player.
   */
  def start(startingPlayer: Player.Value)

  /**
   * Get board visualization.
   */
  def state: State

  /**
   * Make a move, obviously.
   */
  def make(move: Move): Boolean

  def finished: Boolean

  def winner: Option[Player.Value]

  /**
   * Indicates whether this device can alter the board at the moment,
   */
  def locked: Boolean

  /**
   * Get list of available rotations
   */
  def availableRotations: List[Quadrant.Value] = board.availableRotations.toList

  /**
   * Used to mark that data depend on Board version.
   */
  private[logic] class Move {
    private[this] val state = board.version

    def valid = state == board.version
  }

  /**
   * Class used to pass board position.
   *
   * Invalidates after any change in the board layout, such as rotation.
   */
  case class Position(x: Int, y: Int) extends Move

  /**
   * Used to represent rotation move information.
   *
   * Invalidates after any change in the board layout, such as rotation.
   * @param board in form of
   *              1 2
   *              3 4
   * @param r in rotation enumerator counted in degrees clockwise.
   */
  case class Rotation(board: Quadrant.Value, r: QRotation.Value) extends Move
}
