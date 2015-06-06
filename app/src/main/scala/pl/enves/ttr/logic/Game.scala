package pl.enves.ttr.logic

import java.security.InvalidParameterException

import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.utils.Logging

/**
 * Wrapper for game logic.
 */
object Game extends Logging {
  private[this] var board: Option[Board] = None
  /**
   * This field may be set externally. Represents current player.
   */
  var player: Player.Value = Player.X

  def start(startingPlayer: Player.Value) = {
    log("Creating new game")
    log(s"Starting player: $player")
    board = Some(new Board)
    player = startingPlayer
  }

  def winner: Option[Player.Value] = board.get.finished

  def state: Seq[Seq[Option[Player.Value]]] = board.get.lines
  /**
   * Makes a move, whether it's a rotation or putting symbol.
   * After it switches to next player.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  def make(move: Move): Boolean = {
    if (!move.valid) throw new InvalidParameterException("Given move has expired!")
    if (winner.isDefined) throw new GameFinished("Game is finished")

    log(s"Move: $move for $player")

    val res = move match {
      case Position(x, y) => board.get move (x, y, player)
      case Rotation(b, r) => board.get rotate (b, r)
    }

    player = if (player == Player.X) Player.O else Player.X
    log(s"Player changed to $player")

    return res
  }

  private[logic] def boardVersion = board.get.version
}
