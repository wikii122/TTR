package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.{QRotation, Quadrant}

import scala.collection.mutable

/**
 * Field 3x3 with _fields, ability to set them and rotate.
 * Version without saving, safety checks, prints, and nice but time costly things
 * With ability to reverse moves
 * TODO: Optimize more
 */
class LightBoardQuadrant extends Logging {
  private val rotationIndicator = 3
  private[this] val _fields = Array.fill[Int](3, 3)(0)
  private[this] var rotation = QRotation.r0
  private[this] var rotationCooldown = rotationIndicator
  private[this] var oldRotationCooldowns = mutable.Stack[Int]()

  def move(xv: Int, yv: Int, player: Int) = {
    setField(xv, yv, player)
  }

  def unMove(xv: Int, yv: Int, player: Int) = {
    setField(xv, yv, LightField.None)
  }

  def rotate(rot: QRotation.Value) = {
    //Substitution is an irreversible operation
    oldRotationCooldowns.push(rotationCooldown)
    rotationCooldown = rotationIndicator

    rotation = rotation add rot
  }

  def unRotate(rot: QRotation.Value) = {
    rotationCooldown = oldRotationCooldowns.pop()

    rotation = rotation sub rot
  }

  def get(xv: Int, yv: Int): Int = getField(xv, yv)

  def canRotate = rotationCooldown <= 0

  /**
   * Must be called *AFTER* rotate function to work correctly.
   * This was created as separate function because quadrant is not aware of about ¾ moves that
   * happen on the board.
   */
  def tickCooldown() = rotationCooldown = rotationCooldown - 1

  def unTickCooldown() = rotationCooldown = rotationCooldown + 1

  private def getField(y: Int, x: Int): Int = rotation match {
    case QRotation.r0 => _fields(x)(y)
    case QRotation.r90 => _fields(Quadrant.size - y - 1)(x)
    case QRotation.r180 => _fields(Quadrant.size - x - 1)(Quadrant.size - y - 1)
    case QRotation.r270 => _fields(y)(Quadrant.size - x - 1)
  }

  private def setField(y: Int, x: Int, value: Int): Unit = rotation match {
    case QRotation.r0 => _fields(x)(y) = value
    case QRotation.r90 => _fields(Quadrant.size - y - 1)(x) = value
    case QRotation.r180 => _fields(Quadrant.size - x - 1)(Quadrant.size - y - 1) = value
    case QRotation.r270 => _fields(y)(Quadrant.size - x - 1) = value
  }

  private[ai] def setRotation(i: Int) = rotation = QRotation(i)

  private[ai] def getRotation = rotation

  private[ai] def setCooldown(i: Int) = rotationCooldown = i

  private[ai] def getCooldown = rotationCooldown

  private[ai] def setOldCooldowns(stack: mutable.Stack[Int]) = oldRotationCooldowns = stack

  private[ai] def getOldCooldowns = oldRotationCooldowns

  private[ai] def fields = _fields
}

object LightBoardQuadrant {
  def apply() = new LightBoardQuadrant
}
