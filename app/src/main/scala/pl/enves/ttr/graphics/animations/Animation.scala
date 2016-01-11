package pl.enves.ttr.graphics.animations

abstract class Animation(animationTime: Float) {
  private var running = false
  private var elapsed = 0.0f

  def isRunning = running

  def start(): Unit = {
    if (!running) {
      running = true
      elapsed = 0.0f
      onStart()
    }
  }

  def stop(): Unit = {
    if (running) {
      running = false
      onStop()
    }
  }

  def pause(p: Boolean): Unit = {
    if (running == p) {
      running = !p
      onPause(p)
    }
  }

  def animate(dt: Float): Unit = {
    if (running) {
      onAnimate(dt, elapsed)
      elapsed += dt

      if (elapsed >= animationTime) {
        stop()
      }
    }
  }

  protected def onStart(): Unit = {}

  protected def onStop(): Unit = {}

  protected def onPause(p: Boolean): Unit = {}

  protected def onAnimate(dt: Float, elapsed: Float): Unit
}
