package pl.enves.ttr.logic.bot

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.QRotation

import scala.collection.mutable

/**
 * Takes care of cooldown
 */
class BoardQuadrantModel extends Logging {
  private[this] var cooldown = BoardQuadrantModel.rotationIndicator
  private[this] val oldCooldowns = mutable.Stack[Int]()

  def rotate(rot: QRotation.Value) = {
    //Substitution is an irreversible operation
    oldCooldowns.push(cooldown)
    cooldown = BoardQuadrantModel.rotationIndicator
  }

  def unRotate(rot: QRotation.Value) = {
    cooldown = oldCooldowns.pop()
  }

  def canRotate = cooldown <= 0

  /**
   * Must be called *AFTER* rotate function to work correctly.
   * This was created as separate function because quadrant is not aware of about ¾ moves that
   * happen on the board.
   */
  def tickCooldown() = cooldown = cooldown - 1

  def unTickCooldown() = cooldown = cooldown + 1

  private[bot] def setCooldown(i: Int) = cooldown = i

  /**
   * should return value between 0 and rotationIndicator
   */
  private[bot] def getCooldown = if (cooldown > 0) cooldown else 0
}

object BoardQuadrantModel {
  val rotationIndicator = 3
  def apply() = new BoardQuadrantModel
}
