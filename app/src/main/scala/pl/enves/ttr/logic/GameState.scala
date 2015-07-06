package pl.enves.ttr.logic

import pl.enves.androidx.DataManager

/**
 * Object used to load last played game
 */
object GameState extends DataManager("TTR_GAME_STATE") {
  def store(game: Game) = ???

  def load(): Game = ???

  def clear() = ???

  def data: Map[String, String] = ???

  def isActive: Boolean = ???
}
