package pl.enves.ttr.logic

import java.security.InvalidParameterException

import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.Logging

/**
 * Wrapper for game logic.
 */
class StandardGame extends Game with Logging {
  private[this] val board = new Board

  def start(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: ${_player}")
    _player = startingPlayer
  }

  def winner: Option[Player.Value] = board.winner

  def state: State = board.lines

  /**
   * Makes a move, whether it's a rotation or putting symbol.
   * After it switches to next player.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  def make(move: Move): Boolean = {
    implicit val player = this.player
    if (!move.valid) throw new InvalidParameterException("Given move has expired!")
    if (winner.isDefined) throw new GameWon("Game is finished")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board move (x, y)
      case Rotation(b, r) => board rotate (b, r)
    }

    _player = if (player == Player.X) Player.O else Player.X
    log(s"Player set to ${_player}")

    return res
  }

  def finished = board.finished

  protected def boardVersion = board.version

  def locked: Boolean = false
}
