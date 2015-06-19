package pl.enves.ttr.logic
package inner

import pl.enves.ttr.logic.Quadrant
import pl.enves.ttr.utils.Logging

/**
 * Field 3x3 with fields, ability to set them and rotate.
 */
private[inner] class BoardQuadrant extends Logging {
  private[this] val fields = Array.fill[Option[Player.Value]] (3, 3) (None)
  private[this] var rotation = 0

  def move(xv: Int, yv: Int, player: Player.Value) = {
    log(s"Move $player at ($xv, $yv)")
    val (x, y) = readCoordinates(xv % Quadrant.size, yv % Quadrant.size)
    log(s"Coordinates translated to ($x, $y) inside Quadrant")

    if (fields(x)(y).isEmpty) fields(x)(y) = Some(player)
    else throw new FieldTaken("Field is already taken")
  }

  def rotate(rot: QRotation.Value) = {
    log(s"Rotate with $rot")
    val mod = rot match {
      case QRotation.r90 => 1
      case QRotation.r180 => 2
      case QRotation.r270 => 3
      case _ => 0
    }

    rotation = (rotation + mod) % 4
  }

  def get(xv: Int, yv: Int): Option[Player.Value] = {
    val (x, y) = readCoordinates(xv % Quadrant.size, yv % Quadrant.size)

    return fields(x)(y)
  }

  // Lines are horizontal, and assumption is they are vertical, thus x and y must be swapped
  private def readCoordinates(y: Int, x: Int): (Int, Int) = rotation match {
    case 0 => (x, y)
    case 1 => (Quadrant.size - y - 1, x)
    case 2 => (Quadrant.size - x - 1, Quadrant.size - y - 1)
    case 3 => (y, Quadrant.size - x - 1)
  }

  def line(y: Int): Seq[Option[Player.Value]] = for (x <- 0 until Quadrant.size) yield get(x, y)
}

object BoardQuadrant {
  def apply() = new BoardQuadrant
  def apply(loc: Quadrant.Value) = (loc, new BoardQuadrant)

  // Workaround for an unambiguity
  def named(loc: Quadrant.Value) = apply(loc: Quadrant.Value)
}