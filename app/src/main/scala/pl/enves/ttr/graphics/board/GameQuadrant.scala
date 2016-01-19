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
class GameQuadrant(game: Game, quadrant: Quadrant.Value, resources: Resources) extends SceneObject with Logging with Algebra {

  private[this] var rotationOld = game.quadrantRotation(quadrant)

  private[this] var rotationAnimation: Option[QuadrantRotation] = None

  private[this] val fields = Array.tabulate[GameField](Quadrant.size, Quadrant.size) {
    (x, y) => {
      val nx = x + Quadrant.offset(quadrant)._1
      val ny = y + Quadrant.offset(quadrant)._2
      new GameField(game, quadrant, nx, ny, resources)
    }
  }

  for (x <- 0 until Quadrant.size) {
    for (y <- 0 until Quadrant.size) {
      addChild(fields(x)(y))
    }
  }

  override def onUpdateResources(screenRatio: Float): Unit = {
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

  override def onAnimate(dt: Float): Unit = {
    val rotationNew = game.quadrantRotation(quadrant)
    val rotationDiff = rotationNew sub rotationOld

    val animateChange = rotationDiff == QRotation.r0
    var x = 0
    var y = 0
    while (x < Quadrant.size) {
      y = 0
      while (y < Quadrant.size) {
        fields(x)(y).setValue(game.quadrantField(quadrant, x, y), animateChange)
        y += 1
      }
      x += 1
    }

    if (rotationDiff != QRotation.r0) {
      //TODO: Consider 180 degrees rotations
      val ccw = if (rotationDiff == QRotation.r90) true else false
      rotationAnimation.get.setCCW(ccw)

      rotationAnimation.get.start()

      x = 0
      while (x < Quadrant.size) {
        y = 0
        while (y < Quadrant.size) {
          fields(x)(y).stopAnimations()
          y += 1
        }
        x += 1
      }
    }
    rotationOld = rotationNew

    rotationAnimation.get.animate(dt)
  }

  def setWinning(x: Int, y: Int) = fields(x)(y).setWinning(true)
}
