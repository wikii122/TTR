package pl.enves.ttr.logic
package inner

import pl.enves.ttr.logic.Quadrant
import pl.enves.ttr.utils.Logging

/**
 * Field 3x3 with fields, ability to set them and rotate.
 */
class BoardQuadrant extends Logging {
  private[this] val fields = Array.fill[Option[Player.Value]] (3, 3) (None)
  private[this] var rotation = 0

  def move(xv: Int, yv: Int, player: Player.Value) = {
    log(s"Move $player at ($xv, $yv)")
    val (x, y) = readCoordinates(xv % Quadrant.size, yv % Quadrant.size)
    log(s"Coordinates translated to ($x, $y) inside Quadrant")

    if (fields(x)(y).isEmpty) fields(x)(y) = Some(player)
    else throw new FieldTaken("Field is already taken")
  }

  def rotate(rot: Rotation.Value) = {
    log(s"Rotate with $rot")
    val mod = rot match {
      case Rotation.r90 => 1
      case Rotation.r180 => 2
      case Rotation.r270 => 3
      case _ => 0
    }

    rotation = (rotation + mod) % 4
  }

  // Lines are horizontal, and assumption is they are vertical, thus x and y must be swapped
  private def readCoordinates(y: Int, x: Int): (Int, Int) = rotation match {
    case 0 => (x, y)
    case 1 => (Quadrant.size - y, x)
    case 2 => (Quadrant.size - x, Quadrant.size - y)
    case 3 => (y, Quadrant.size - x)
  }

  def line(i: Int): Seq[Option[Player.Value]] = fields(i).toSeq
}

object BoardQuadrant {
  def apply() = new BoardQuadrant
  def apply(loc: Quadrant.Value) = (loc, new BoardQuadrant)

  // Workaround for an unambiguity
  def named(loc: Quadrant.Value) = apply(loc: Quadrant.Value)
}