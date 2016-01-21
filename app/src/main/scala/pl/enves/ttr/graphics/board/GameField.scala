package pl.enves.ttr.graphics.board

import pl.enves.ttr.graphics.Resources
import pl.enves.ttr.graphics.animations.{FieldRotation, Shake}
import pl.enves.ttr.logic._

class GameField(game: Game, quadrant: Quadrant.Value, boardX: Int, boardY: Int)
  extends Field(quadrant) with Illegal {

  private[this] var shakeAnimation: Option[Shake] = None
  private[this] var rotationAnimation: Option[FieldRotation] = None

  def setValue(v: Option[Player.Value], animateChange: Boolean): Unit = {
    if (animateChange && v != getValue) {
      shakeAnimation.get.stop()
      rotationAnimation.get.start()
    }
    setValue(v)
  }

  override protected def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    super.onUpdateResources(resources, screenRatio)

    val scale = addScale(1.0f, 1.0f, 1.0f, false)
    val rotation = addRotation(0.0f, 0.0f, 0.0f, 1.0f, false)

    shakeAnimation = Some(new Shake(1.0f, rotation, 15.0f, 5.0f))
    rotationAnimation = Some(new FieldRotation(1.0f, rotation, scale))
  }

  override protected def onAnimate(dt: Float): Unit = {
    shakeAnimation.get.animate(dt)
    rotationAnimation.get.animate(dt)
  }

  override protected def onClick(): Unit = {
    try {
      val move = new Position(boardX, boardY)
      game.make(move)
      discardIllegal()
    } catch {
      case e: FieldTaken =>
        setIllegal()
      case e: BoardLocked =>
        setIllegal()
    }
  }

  def stopAnimations(): Unit = {
    rotationAnimation.get.stop()
    shakeAnimation.get.stop()
  }

  override def discardIllegal(): Unit = {
    shakeAnimation.get.stop()
  }

  override def setIllegal(): Unit = {
    rotationAnimation.get.stop()
    shakeAnimation.get.start()
  }

}
