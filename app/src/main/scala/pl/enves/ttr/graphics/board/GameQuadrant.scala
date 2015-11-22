package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Basic size: 3.0x3.0
 * (0.0, 0.0) - in the middle
 */
class GameQuadrant(game: Game, quadrant: Quadrant.Value, resources: Resources) extends SceneObject with Logging with Algebra {

  val rotationTime = 1.0f //seconds

  var rotationAnimated = false

  var rotationCCW = false

  var rotationLinear = 0.0f

  var rotationOld = game.quadrantRotation(quadrant)

  val fields = Array.fill[BoardField](Quadrant.size, Quadrant.size)(new BoardField(quadrant, resources))
  for (x <- 0 until Quadrant.size) {
    for (y <- 0 until Quadrant.size) {
      // Position from the quadrant center
      // TODO: calculate from Quadrant.size
      val nx = x - 1.0f
      val ny = y - 1.0f
      fields(x)(y).translate(nx, ny, 0.0f)

      addChild(fields(x)(y))
    }
  }

  def quadrantOffset(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0, 0)
    case Quadrant.second => (Quadrant.size, 0)
    case Quadrant.third => (0, Quadrant.size)
    case Quadrant.fourth => (Quadrant.size, Quadrant.size)
  }

  override def onUpdateResources(): Unit = {}

  override protected def onUpdateTheme(): Unit = {}

  def checkWinning(x: Int, y: Int): Boolean = {
    val nx = x + quadrantOffset(quadrant)._1
    val ny = y + quadrantOffset(quadrant)._2
    game.finished && game.finishingMove != Nil && game.finishingMove.contains((ny, nx))
  }

  override def onAnimate(dt: Float): Unit = {
    this.synchronized {
      for (x <- 0 until Quadrant.size) {
        for (y <- 0 until Quadrant.size) {
          fields(x)(y).value = game.quadrantField(quadrant, x, y)
        }
      }

      val rotationNew = game.quadrantRotation(quadrant)
      val rotationDiff = rotationNew sub rotationOld

      if(rotationDiff != QRotation.r0) {
        rotationAnimated = true

        rotationLinear = 0.0f

        //TODO: Consider 180 degrees rotations
        rotationCCW = if(rotationDiff == QRotation.r90) true else false

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
        fields(x)(y).winning = checkWinning(x, y)
      }
    }

    if (rotationAnimated) {
      rotationLinear += dt / rotationTime

      if (rotationLinear >= 1.0f) {
        rotationLinear = 1.0f
        rotationAnimated = false
      }

      objectRotationAngle = ((Math.sin(Math.PI / 2 + rotationLinear * Math.PI) + 1.0) * 45.0).toFloat

      if (rotationCCW) {
        objectRotationAngle = -objectRotationAngle
      }

      val a = Math.sqrt(2) / (2 * Math.cos(Math.toRadians(45.0f - Math.abs(objectRotationAngle))))
      objectScale = Array(a.toFloat, a.toFloat, 1.0f)
    }
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

  def processClick(fx: Float, fy: Float): Boolean = {
    log("Click " + quadrant.toString + " " + fx + " " + fy)

    //Board is just a square
    if (fx > -1.5f && fx < 1.5f && fy > -1.5f && fy < 1.5f) {
      // Calculate field
      // Although quadrants are now independent, fields inside them are still not
      val x = Math.floor(fx + 1.5f).toInt
      val y = Math.floor(fy + 1.5f).toInt
      val nx = x + quadrantOffset(quadrant)._1
      val ny = y + quadrantOffset(quadrant)._2
      try {
        val move = new game.Position(nx, ny)
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
