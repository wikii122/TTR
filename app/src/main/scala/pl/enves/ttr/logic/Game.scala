package pl.enves.ttr.logic

import pl.enves.ttr.logic.inner.Board

/**
 * Wrapper for game logic.
 */
object Game {
  var board: Option[Board] = None

  def start() = ???

  // Requres parameters
  def move(pos: Position): Boolean = ???

  // Requires parameters
  def rotate(): Boolean = ???

  def finished(): Option[Player.Value] = ???

  def state: List[List[Option[Player.Value]]] = ???

  private[logic] def boardVersion = board.get.version
}
