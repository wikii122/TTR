package pl.enves.ttr.logic
package inner

import pl.enves.ttr.logic.Player

/**
 * Keeps fields states.
 */
class Board {
  case class Field(symbol: Option[Player.Value])
}
