package pl.enves.ttr.logic

/**
 * Available players
 */
object Player extends Enumeration {
  val X = Value
  val O = Value

  implicit class PlayerValueExtension(player: Player.Value) {
    def other = if (player == X) O else X
  }
}
