package pl.enves.ttr.logic.ai

import pl.enves.ttr.logic.Player

object LightField {
  val X = 1
  val O = -1
  val None = 0

  def opponent(player: Int) = if (player == LightField.X) LightField.O else LightField.X

  def fromOption(p: Option[Player.Value]): Int = {
    if(p.isDefined) {
      p.get match {
        case Player.X => LightField.X
        case Player.O => LightField.O
      }
    } else {
      LightField.None
    }
  }
}
