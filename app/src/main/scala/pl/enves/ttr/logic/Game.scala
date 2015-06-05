package pl.enves.ttr.logic

import pl.enves.ttr.logic.inner.Board

/**
 * Wrapper for game logic.
 */
object Game {
  var board: Option[Board] = None

  def start() = ???

  def finished(): Option[Player.Value] = ???

  def state: Seq[Seq[Option[Player.Value]]] = ???

  def make(move: Move) = move match {
    case Position(x, y) => board.get move (x, y)
    case Rotation(b, r) => board.get rotate (b, r)
  }

  private[logic] def boardVersion = board.get.version
}
