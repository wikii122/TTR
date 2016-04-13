package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.animations.QuadrantRotation
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Basic size: 3.0x3.0
 * (0.0, 0.0) - in the middle
 */
class GameQuadrant(makeMove: Move => Unit, quadrant: Quadrant.Value) extends SceneObject with Logging with Algebra {

  private[this] var rotation = QRotation.r0

  private[this] var rotationAnimation: Option[QuadrantRotation] = None

  private[this] val fields = Array.tabulate[GameField](Quadrant.size, Quadrant.size) {
    (x, y) => {
      val nx = x + Quadrant.offset(quadrant)._1
      val ny = y + Quadrant.offset(quadrant)._2
      new GameField(makeMove, quadrant, nx, ny)
    }
  }

  for (x <- 0 until Quadrant.size) {
    for (y <- 0 until Quadrant.size) {
      addChild(fields(x)(y))
    }
  }

  override def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    for (x <- 0 until Quadrant.size) {
      for (y <- 0 until Quadrant.size) {
        // Position from the quadrant center
        // TODO: calculate from Quadrant.size
        val nx = x - 1.0f
        val ny = y - 1.0f
        fields(x)(y).addTranslation(nx / 0.9f, ny / 0.9f, 0.0f, true)
      }
    }

    val scale = addScale(0.9f, 0.9f, 1.0f, true)
    val rotation = addRotation(0.0f, 0.0f, 0.0f, 1.0f, false)

    rotationAnimation = Some(new QuadrantRotation(1.0f, rotation, scale))
  }

  override protected def onSyncState(game: Game): Unit = {
    rotation = game.quadrantRotation(quadrant)
  }

  override def onAnimate(dt: Float): Unit = {
    rotationAnimation.get.animate(dt)
  }

  def startRotationAnimation(rotation: QRotation.Value): Unit = {
    val ccw = rotation != QRotation.r90
    rotationAnimation.get.setCCW(ccw)
    rotationAnimation.get.start()

    var x = 0
    var y = 0
    while (x < Quadrant.size) {
      y = 0
      while (y < Quadrant.size) {
        fields(x)(y).stopAnimations()
        y += 1
      }
      x += 1
    }
  }

  def setWinning(x: Int, y: Int) = fields(x)(y).setWinning(true)

  def getGameField(x: Int, y: Int): GameField = fields(x)(y)

  def getRotation: QRotation.Value = rotation
}
