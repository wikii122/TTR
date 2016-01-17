package pl.enves.ttr.graphics.board

import android.content.Context
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.animations.QuadrantRotation
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Basic size: 3.0x3.0
 * (0.0, 0.0) - in the middle
 */
class GameQuadrant(context: Context with GameManager, quadrant: Quadrant.Value, resources: Resources) extends SceneObject with Logging with Algebra {

  private[this] var rotationOld = context.game.quadrantRotation(quadrant)

  private[this] var rotationAnimation: Option[QuadrantRotation] = None

  private[this] val fields = Array.fill[Field](Quadrant.size, Quadrant.size)(new Field(quadrant, resources))
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

  override protected def onUpdateTheme(): Unit = {}

  private def checkWinning(x: Int, y: Int): Boolean = {
    val game = context.game
    val nx = x + Quadrant.offset(quadrant)._1
    val ny = y + Quadrant.offset(quadrant)._2
    game.finished && game.finishingMove != Nil && game.finishingMove.contains((ny, nx))
  }

  override def onAnimate(dt: Float): Unit = {
    this.synchronized {
      val rotationNew = context.game.quadrantRotation(quadrant)
      val rotationDiff = rotationNew sub rotationOld

      val animateChange = rotationNew == rotationOld
      for (x <- 0 until Quadrant.size) {
        for (y <- 0 until Quadrant.size) {
          fields(x)(y).setValue(context.game.quadrantField(quadrant, x, y), animateChange)
        }
      }

      if (rotationDiff != QRotation.r0) {
        //TODO: Consider 180 degrees rotations
        val ccw = if (rotationDiff == QRotation.r90) true else false
        rotationAnimation.get.setCCW(ccw)

        rotationAnimation.get.start()

        for (x <- 0 until Quadrant.size) {
          for (y <- 0 until Quadrant.size) {
            fields(x)(y).discardIllegal()
          }
        }
      }
      rotationOld = rotationNew
    }

    //This doesn't have to be synchronized
    for (x <- 0 until Quadrant.size) {
      for (y <- 0 until Quadrant.size) {
        fields(x)(y).setWinning(checkWinning(x, y))
      }
    }

    rotationAnimation.get.animate(dt)
  }

  override def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {

  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    try {
      val (near, far) = unProjectMatrices(mvMatrix.get(), pMatrix.get(), clickX, clickY, viewport)
      val I = intersectRayAndXYPlane(near, far)
      return processClick(I(0), I(1))
    } catch {
      case e: UnProjectException =>
        error(e.getMessage)
        return false
      case e: IntersectException =>
        // In current scene configuration this shouldn't happen
        error(e.getMessage)
        return false
    }
  }

  private def processClick(ffx: Float, ffy: Float): Boolean = {
    val fx = ffx * 0.9f
    val fy = ffy * 0.9f
    //Board is just a square
    if (fx > -1.5f && fx < 1.5f && fy > -1.5f && fy < 1.5f) {
      // Calculate field
      // Although quadrants are now independent, fields inside them are still not
      var x = Math.floor(fx + 1.5f).toInt
      var y = Math.floor(fy + 1.5f).toInt

      //shit happens
      if (x >= Quadrant.size) x = Quadrant.size - 1
      if (y >= Quadrant.size) y = Quadrant.size - 1
      if (x < 0) x = 0
      if (y < 0) y = 0

      val nx = x + Quadrant.offset(quadrant)._1
      val ny = y + Quadrant.offset(quadrant)._2
      try {
        val game = context.game
        val move = new Position(nx, ny)
        game.make(move)
        fields(x)(y).discardIllegal()
      } catch {
        case e: FieldTaken =>
          fields(x)(y).setIllegal()
        case e: BoardLocked =>
          fields(x)(y).setIllegal()
      }
      return true
    }
    return false
  }
}
