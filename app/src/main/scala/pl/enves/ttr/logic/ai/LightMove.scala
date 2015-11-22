package pl.enves.ttr.logic.ai

import pl.enves.ttr.logic.QRotation

class LightMove

case class LightPosition(quadrant: Int, x: Int, y: Int) extends LightMove

case class LightRotation(quadrant: Int, r: QRotation.Value) extends LightMove
