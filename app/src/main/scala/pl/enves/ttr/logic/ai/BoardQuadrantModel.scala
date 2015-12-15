package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.QRotation

import scala.collection.mutable

/**
 * Takes care of cooldown
 */
class BoardQuadrantModel extends Logging {
  private val rotationIndicator = 3
  private[this] var rotationCooldown = rotationIndicator
  private[this] var oldRotationCooldowns = mutable.Stack[Int]()

  def rotate(rot: QRotation.Value) = {
    //Substitution is an irreversible operation
    oldRotationCooldowns.push(rotationCooldown)
    rotationCooldown = rotationIndicator
  }

  def unRotate(rot: QRotation.Value) = {
    rotationCooldown = oldRotationCooldowns.pop()
  }

  def canRotate = rotationCooldown <= 0

  /**
   * Must be called *AFTER* rotate function to work correctly.
   * This was created as separate function because quadrant is not aware of about ¾ moves that
   * happen on the board.
   */
  def tickCooldown() = rotationCooldown = rotationCooldown - 1

  def unTickCooldown() = rotationCooldown = rotationCooldown + 1

  private[ai] def setCooldown(i: Int) = rotationCooldown = i

  private[ai] def getCooldown = rotationCooldown

  private[ai] def setOldCooldowns(stack: mutable.Stack[Int]) = oldRotationCooldowns = stack

  private[ai] def getOldCooldowns = oldRotationCooldowns
}

object BoardQuadrantModel {
  def apply() = new BoardQuadrantModel
}
