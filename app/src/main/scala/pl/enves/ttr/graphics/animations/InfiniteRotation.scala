package pl.enves.ttr.graphics.animations

import pl.enves.ttr.graphics.transformations.Rotation

class InfiniteRotation(rotation: Rotation, omega: Float)
  extends Animation(666.6f) {

  private[this] val clockwise = Math.random() < 0.5

  override protected def onAnimate(dt: Float, elapsed: Float): Unit = {
    val a = if (clockwise) omega * elapsed else -omega * elapsed

    val angle = a % 360.0f

    rotation.setA(rotation.getDefaultA + angle)
  }

  override protected def onStart(): Unit = {
    rotation.setEnabled(true)
  }

  override protected def onPause(p: Boolean): Unit = {
    if (p) {
      rotation.reset()
      rotation.setEnabled(false)
    } else {
      rotation.setEnabled(true)
    }
  }

  override protected def onStop(): Unit = {
    start()
  }
}
