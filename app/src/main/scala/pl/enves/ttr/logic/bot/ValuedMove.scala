package pl.enves.ttr.logic.bot

import pl.enves.ttr.logic.Move

case class ValuedMove(value: Int, move: Move) extends Ordered[ValuedMove] {
  def compare(that: ValuedMove) = that.value - value
}
