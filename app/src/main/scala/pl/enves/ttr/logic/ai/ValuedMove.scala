package pl.enves.ttr.logic.ai

case class ValuedMove(value: Int, move: LightMove) extends Ordered[ValuedMove] {
  def compare(that: ValuedMove) = that.value - value
}
