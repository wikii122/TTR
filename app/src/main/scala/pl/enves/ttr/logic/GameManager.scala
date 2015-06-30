package pl.enves.ttr.logic

import pl.enves.ttr.utils.Logging

trait GameManager extends Logging {
  protected[this] var _game: Option[Game] = None

  def game = _game.get

  def game_=(game: Game) = if (_game.isEmpty) _game = Some(game)
    else throw new UnsupportedOperationException("Game is already initialized")
}
