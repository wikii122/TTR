package pl.enves.ttr.logic
package inner

import pl.enves.ttr.logic.Player

/**
 * Keeps fields states.
 */
private[logic] class Board {
  private[this] var _version = 0

  def version: Int = _version

  // Requres parameters
  def move(x: Int, y: Int): Boolean = ???

  // Requires parameters
  def rotate(quadrant: Quadrant.Value, rotation: Rotation.Value): Boolean = ???
}
