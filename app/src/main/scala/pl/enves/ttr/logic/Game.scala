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
}
