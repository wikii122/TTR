package pl.enves.ttr.logic
package inner

import pl.enves.ttr.logic.Player

/**
 * Keeps fields states.
 */
class Board {
  private[this] var _version = 0

  def version: Int = _version

  case class Field(symbol: Option[Player.Value])
}
