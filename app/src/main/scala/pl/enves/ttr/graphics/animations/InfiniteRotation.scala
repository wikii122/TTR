package pl.enves.ttr.graphics.animations

import pl.enves.ttr.graphics.transformations.Rotation

class InfiniteRotation(rotation: Rotation, omega: Float)
  extends Animation(666.6f) {

  override protected def onAnimate(dt: Float, elapsed: Float): Unit = {
    var angle = rotation.getA + omega * dt

    if (angle >= 360.0f) {
      angle = 360.0f - angle
    }

    if (angle <= -360.0f) {
      angle = -360.0f + angle
    }

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
