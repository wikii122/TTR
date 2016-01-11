package pl.enves.ttr.graphics.animations

import pl.enves.ttr.graphics.transformations.{Rotation, Scale}

class QuadrantRotation(animationTime: Float, rotation: Rotation, scale: Scale)
  extends Animation(animationTime) {

  var ccw = false

  def setCCW(c: Boolean) = ccw = c

  override protected def onAnimate(dt: Float, elapsed: Float): Unit = {
    val a = ((Math.sin(Math.PI / 2 + elapsed * Math.PI) + 1.0) * 45.0).toFloat
    val angle = if (ccw) -a else a
    rotation.setA(rotation.getDefaultA + angle)

    val s = rotatedSquareScale(angle)
    scale.setX(scale.getDefaultX * s)
    scale.setY(scale.getDefaultY * s)
  }

  override protected def onStart(): Unit = {
    rotation.setEnabled(true)
    scale.setEnabled(true)
  }

  override protected def onStop(): Unit = {
    rotation.reset()
    scale.reset()
    rotation.setEnabled(false)
    scale.setEnabled(false)
  }


  def rotatedSquareScale(angle: Float): Float = {
    return (Math.sqrt(2) / (2 * Math.cos(Math.toRadians(45.0f - Math.abs(angle % 90.0f))))).toFloat
  }
}
