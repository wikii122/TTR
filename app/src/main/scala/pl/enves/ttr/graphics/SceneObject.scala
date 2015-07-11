package pl.enves.ttr.graphics

/**
 *
 */
trait SceneObject {
  def onUpdateResources() : Unit
  def onAnimate(dt: Float): Unit
  def onDraw(): Unit
  def onClick(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean
}
