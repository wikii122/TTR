package pl.enves.ttr.graphics.animations

import pl.enves.ttr.graphics.transformations.Rotation

class Shake(animationTime: Float, rotation: Rotation, amplitude: Float, frequency: Float)
  extends Animation(animationTime) {

  override protected def onAnimate(dt: Float, elapsed: Float): Unit = {
    val s = amplitude * Math.sin(elapsed * frequency * 2 * Math.PI).toFloat
    val angle = rotation.getDefaultA + s * Math.sin((elapsed / animationTime) * Math.PI).toFloat
    rotation.setA(angle)
  }

  override protected def onStart(): Unit = {
    rotation.setEnabled(true)
  }

  override protected def onStop(): Unit = {
    rotation.reset()
    rotation.setEnabled(false)
  }
}
