package pl.enves.ttr.logic

import java.security.InvalidParameterException

import pl.enves.ttr.logic.inner.Board

/**
 * Wrapper for game logic.
 */
object Game {
  private[this] var board: Option[Board] = None
  /**
   * This field may be set externally. Represents current player.
   */
  var player: Player.Value = Player.X

  def start(startingPlayer: Player.Value) = {
    board = Some(new Board)
    player = startingPlayer
  }

  def finished: Option[Player.Value] = ???

  def state: Seq[Seq[Option[Player.Value]]] = ???

  /**
   * Makes a move, whether it's a rotation or putting symbol.
   * After it switches to next player.
   * May throw InvalidParameterException when given invalid move,
   * and ImpossibleMove when Position is taken or game finished.
   * If called before start, throws NoSuchElementException.
   */
  def make(move: Move): Boolean = {
    if (!move.valid) throw new InvalidParameterException("Given move has expired!")
    if (finished.isDefined) throw new MoveImpossible("Game has finished")
    val res = move match {
      case Position(x, y) => board.get move (x, y, player)
      case Rotation(b, r) => board.get rotate (b, r)
    }

    player = if (player == Player.X) Player.O else Player.X

    return res
  }

  private[logic] def boardVersion = board.get.version
}
