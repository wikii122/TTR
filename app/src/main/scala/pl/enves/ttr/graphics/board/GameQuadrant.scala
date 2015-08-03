package pl.enves.ttr.graphics.board

import android.opengl.Matrix
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Basic size: 3.0x3.0
 * (0.0, 0.0) - in the middle
 */
class GameQuadrant(game: Game, quadrant: Quadrant.Value, resources: Resources) extends SceneObject with Logging with Algebra {

  var square: Option[Geometry] = None

  var ring: Option[Int] = None
  var cross: Option[Int] = None
  var empty: Option[Int] = None

  var maskShader: Option[MaskShader] = None

  //TODO: Load from settings
  var crossColor = Array(27.0f / 255.0f, 20.0f / 255.0f, 100.0f / 255.0f, 1.0f)
  var ringColor = Array(27.0f / 255.0f, 20.0f / 255.0f, 100.0f / 255.0f, 1.0f)
  var outerColor1 = Array(179.0f / 255.0f, 179.0f / 255.0f, 179.0f / 255.0f, 1.0f)
  var outerColor2 = Array(255.0f / 255.0f, 255.0f / 255.0f, 255.0f / 255.0f, 1.0f)
  var winnerOuterColor = Array(0.0f / 255.0f, 179.0f / 255.0f, 0.0f / 255.0f, 1.0f)
  var illegalOuterColor = Array(179.0f / 255.0f, 0.0f / 255.0f, 0.0f / 255.0f, 1.0f)

  val illegalHighlightTime: Long = 2000
  var illegalHighlightTimeSet: Long = 0

  val noColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  def quadrantOffset(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0, 0)
    case Quadrant.second => (3, 0)
    case Quadrant.third => (0, 3)
    case Quadrant.fourth => (3, 3)
  }

  def quadrantFields(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0 to 2, 0 to 2)
    case Quadrant.second => (0 to 2, 3 to 5)
    case Quadrant.third => (3 to 5, 0 to 2)
    case Quadrant.fourth => (3 to 5, 3 to 5)
  }

  override def onUpdateResources(): Unit = {
    square = Some(resources.getGeometry(DefaultGeometryId.Square.toString))

    ring = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskRing.toString))
    cross = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskCross.toString))
    empty = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskEmpty.toString))

    maskShader = Some(resources.getShader(ShaderId.Mask).asInstanceOf[MaskShader])
  }

  def defaultOuterColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => outerColor1
    case Quadrant.second => outerColor2
    case Quadrant.third => outerColor2
    case Quadrant.fourth => outerColor1
  }

  def checkWinning(x: Int, y: Int): Boolean = {
    game.finished && game.finishingMove != Nil && game.finishingMove.contains((y, x))
  }

  def checkIllegal(x: Int, y: Int): Boolean = {
    (x, y) == illegalCoords && System.currentTimeMillis() < illegalHighlightTimeSet + illegalHighlightTime
  }

  def discardIllegal(): Unit = {
    illegalHighlightTimeSet -= illegalHighlightTime
  }

  def setIllegal(x: Int, y: Int): Unit = {
    illegalHighlightTimeSet = System.currentTimeMillis()
    illegalCoords = (x, y)
  }

  var illegalCoords = (0, 0)

  def drawFigure(player: Option[Player.Value], x: Int, y: Int): Unit = {
    MVMatrix.push()

    // From the quadrant center
    val nx = x % 3 - 1.0f
    val ny = y % 3 - 1.0f
    Matrix.translateM(MVMatrix(), 0, nx, ny, 0.0f)

    val outer = if (checkIllegal(x, y)) {
      illegalOuterColor
    } else {
      if (checkWinning(x, y)) {
        winnerOuterColor
      } else {
        defaultOuterColor(quadrant)
      }
    }

    if (player.isDefined) {
      if (player.get == Player.O) {
        maskShader.get.draw(square.get, (noColor, ringColor, outer, ring.get))
      }
      if (player.get == Player.X) {
        maskShader.get.draw(square.get, (noColor, crossColor, outer, cross.get))
      }
    } else {
      maskShader.get.draw(square.get, (noColor, noColor, outer, empty.get))
    }

    MVMatrix.pop()
  }

  def drawFigures(state: game.State, quadrant: Quadrant.Value) = {
    val fields = quadrantFields(quadrant)
    for (i <- fields._1) {
      for (j <- fields._2) {
        drawFigure(state(i)(j), j, i)
      }
    }
  }

  override def onAnimate(dt: Float): Unit = {
    if (objectRotationAngle >= 2.0f) {
      objectRotationAngle -= 2.0f
    }

    if (objectRotationAngle <= -2.0f) {
      objectRotationAngle += 2.0f
    }

    val a = Math.sqrt(2) / (2 * Math.cos(Math.toRadians(45.0f - Math.abs(objectRotationAngle))))
    objectScale = Array(a.toFloat, a.toFloat, 1.0f)
  }

  override def onDraw(): Unit = {
    val state: game.State = game.state
    drawFigures(state, quadrant)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean = {
    try {
      val (near, far) = unProjectMatrices(MVMatrix(), PMatrix(), clickX, clickY, viewport)
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
      val x = Math.floor(fx + 1.5f).toInt + quadrantOffset(quadrant)._1
      val y = Math.floor(fy + 1.5f).toInt + quadrantOffset(quadrant)._2
      try {
        val move = new game.Position(x, y)
        game.make(move)
        discardIllegal()
      } catch {
        case e: FieldTaken =>
          setIllegal(x, y)
      }
      return true
    }
    return false
  }
}
