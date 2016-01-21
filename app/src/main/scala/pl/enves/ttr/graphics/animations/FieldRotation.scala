package pl.enves.ttr.graphics.animations

import pl.enves.ttr.graphics.transformations.{Rotation, Scale}

class FieldRotation(animationTime: Float, rotation: Rotation, scale: Scale)
  extends Animation(animationTime) {

  private[this] val clockwise = Math.random() < 0.5

  override protected def onAnimate(dt: Float, elapsed: Float): Unit = {
    val angle = ((Math.cos(Math.PI + elapsed * Math.PI).toFloat / 2.0f) + 0.5f) * 360.0f
    val a = if (clockwise) -angle else angle
    rotation.setA(rotation.getDefaultA + a)

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
    return if (angle < 45.0f || angle > 315.0f) {
      (Math.sqrt(2) / (2 * Math.cos(Math.toRadians(45.0f - Math.abs(angle % 90.0f))))).toFloat
    } else {
      0.7071f
    }
  }
}
