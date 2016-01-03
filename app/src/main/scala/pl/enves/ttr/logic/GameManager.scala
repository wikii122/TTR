package pl.enves.ttr.logic

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.games.{AIGame, ReplayStandardGame, ReplayAIGame, StandardGame}

trait GameManager extends Logging {
  protected[this] var _game: Option[Game] = None

  def game = _game.get

  def game_=(game: Game) = if (_game.isEmpty) _game = Some(game)
    else throw new UnsupportedOperationException("Game is already initialized")

  def replayGame(): Unit = {
    if (_game.isDefined) {
      if (game.finished) {
        game.gameType match {
          case Game.STANDARD => _game = Some(ReplayStandardGame(game.asInstanceOf[StandardGame]))
          case Game.AI => _game = Some(ReplayAIGame(game.asInstanceOf[AIGame]))
        }
      } else {
        throw new UnsupportedOperationException("Cannot replay not finished game")
      }
    } else {
      throw new UnsupportedOperationException("Cannot replay empty game")
    }
  }
}
