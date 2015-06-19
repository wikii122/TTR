package pl.enves.ttr.logic

abstract class Game {
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

  protected def boardVersion: Int

  /**
   * Used to mark that data depend on Board version.
   */
  private[logic] class Move {
    private[this] val state = boardVersion

    def valid = state == boardVersion
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
