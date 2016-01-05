package pl.enves.ttr.logic

import pl.enves.androidx.Logging

trait GameManager extends Logging {
  protected[this] var _game: Option[Game] = None

  def game = _game.get

  def game_=(game: Game) = if (_game.isEmpty) _game = Some(game)
  else throw new UnsupportedOperationException("Game is already initialized")

  def replayGame(): Unit = {
    if (_game.isDefined) {
      if (game.finished || game.gameType == Game.REPLAY) {
        game.gameType match {
          case Game.STANDARD => _game = Some(ReplayGame(game.asInstanceOf[StandardGame]))
          case Game.AI => _game = Some(ReplayGame(game.asInstanceOf[AIGame]))
          case Game.GPS_MULTIPLAYER => //TODO
          case Game.REPLAY => _game = Some(ReplayGame(game.asInstanceOf[ReplayGame]))
        }
      } else {
        throw new UnsupportedOperationException("Cannot replay not finished game")
      }
    } else {
      throw new UnsupportedOperationException("Cannot replay empty game")
    }
  }
}
