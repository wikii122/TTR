package pl.enves.ttr.logic
package inner

import pl.enves.androidx.Logging
import pl.enves.ttr.utils.JsonMappable
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * Field 3x3 with _fields, ability to set them and rotate.
 */
private[inner] class BoardQuadrant extends Logging with JsonMappable {
  private val rotationIndicator = 3
  private val _fields = Array.fill[Option[Player.Value]] (3, 3) (None)
  private var _rotation = QRotation.r0
  private var _cooldown = rotationIndicator

  def move(xv: Int, yv: Int, player: Player.Value) = {
    log(s"Move $player at ($xv, $yv)")
    val x = readCoordinateX(xv, yv)
    val y = readCoordinateY(xv, yv)
    log(s"Coordinates translated to ($x, $y) inside Quadrant")

    if (_fields(x)(y).isEmpty) _fields(x)(y) = Some(player)
    else throw new FieldTaken("Field is already taken")
  }

  def rotate(rot: QRotation.Value) = {
    log(s"Rotate with $rot")
    if (!isRotable) throw new RotationLocked("Rotation cooldown has not expired.")

    _cooldown = rotationIndicator

    _rotation = _rotation add rot
  }

  def get(xv: Int, yv: Int): Option[Player.Value] = {
    val x = readCoordinateX(xv, yv)
    val y = readCoordinateY(xv, yv)

    return _fields(x)(y)
  }

  def sync(data: BoardQuadrant): Unit = {
    _rotation = data._rotation
    _cooldown = data._cooldown

    data._fields.zipWithIndex foreach {
      case (field, i) =>
        _fields(i) = field
    }
  }
  
  def rotation: QRotation.Value = _rotation
  private[inner] def rotation_=(i: Int) = _rotation = QRotation(i)
  
  def line(x: Int): Seq[Option[Player.Value]] = for (y <- 0 until Quadrant.size) yield get(x, y)

  def isRotable = _cooldown == 0

  /**
   * Must be called *AFTER* rotate function to work correctly.
   * This was created as separate function because quadrant is not aware of about ¾ of moves that
   * happen on the board.
   */
  def tickCooldown() = if (_cooldown > 0) _cooldown = _cooldown - 1

  private def readCoordinateX(x: Int, y: Int): Int = _rotation match {
    case QRotation.r0 => x
    case QRotation.r90 => Quadrant.size - y - 1
    case QRotation.r180 => Quadrant.size - x - 1
    case QRotation.r270 => y
  }

  private def readCoordinateY(x: Int, y: Int): Int = _rotation match {
    case QRotation.r0 => y
    case QRotation.r90 => x
    case QRotation.r180 => Quadrant.size - y - 1
    case QRotation.r270 => Quadrant.size - x - 1
  }

  override def toMap: Map[String, Any] = Map(
    "cooldown" -> cooldown,
    // FIXME Arrays are not supported in protocol
    "fields" -> (_fields.toList map { arr => (arr.toList map { p => p.toJson}).toJson }),
    "rotation" -> rotation.id
  )

  private[logic] def fields = _fields
  private[logic] def cooldown = _cooldown
  private[inner] def cooldown_=(i: Int) = _cooldown = i

}

object BoardQuadrant {
  def apply() = new BoardQuadrant
  def apply(loc: Quadrant.Value) = (loc, new BoardQuadrant)

  // Workaround for an unambiguity
  def named(loc: Quadrant.Value) = apply(loc: Quadrant.Value)
}