package pl.enves.ttr.graphics.transformations

/**
 * @param enabled set to false if transformation is used in temporary animation
 */
abstract class Transformation(enabled: Boolean) {
  private[this] var _enabled = enabled

  def isEnabled = _enabled

  def setEnabled(e: Boolean): Unit = _enabled = e | enabled

  def transform(matrix: Array[Float]): Unit = {
    if(_enabled) {
      onTransform(matrix)
    }
  }

  protected def onTransform(matrix: Array[Float]): Unit

  def reset(): Unit = {
    _enabled = enabled
  }
  
  protected def onReset(): Unit
}
