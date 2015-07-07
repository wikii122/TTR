package pl.enves.ttr.graphics

import android.opengl.Matrix
import pl.enves.ttr.graphics.shaders.{ColorShaderData, TextureShaderData}
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.{Algebra, Logging}

/**
 * Game board
 * Uses 2 coordinate systems:
 * Logic: (Int, Int), (0, 0) is where (0, 0) field is
 * Display: (Float, Float), (0, 0) is in the center of displayed board
 */
class GameBoard(game: Game, resources: Resources) extends Logging with Algebra {

  private class ClickException(msg: String) extends RuntimeException(msg)

  private abstract class BoardZone()

  private case class NoneZone() extends BoardZone()

  private case class FigureZone() extends BoardZone()

  private case class ArrowZone(quadrant: Quadrant.Value, rotation: QRotation.Value) extends BoardZone()

  val playerText = new StaticText("Player:", resources, 0.75f, 0.25f)

  val board3x3 = resources.getGeometry(resources.ModelId.Board3x3)
  val rectangle = resources.getGeometry(resources.ModelId.Rectangle)

  val arrowLeft = new TextureShaderData(resources.getTexture(resources.TextureId.ArrowLeft))
  val arrowRight = new TextureShaderData(resources.getTexture(resources.TextureId.ArrowRight))
  val arrowLeftGray = new TextureShaderData(resources.getTexture(resources.TextureId.ArrowLeftGray))
  val arrowRightGray = new TextureShaderData(resources.getTexture(resources.TextureId.ArrowRightGray))
  val ring = new TextureShaderData(resources.getTexture(resources.TextureId.Ring))
  val cross = new TextureShaderData(resources.getTexture(resources.TextureId.Cross))

  val colorShader = resources.getShader(resources.ShaderId.Color)
  val colorsShader = resources.getShader(resources.ShaderId.Colors)
  val textureShader = resources.getShader(resources.ShaderId.Texture)

  val winningHighlight = new ColorShaderData(Array(0.0f, 1.0f, 0.0f, 1.0f))
  val illegalHighlight = new ColorShaderData(Array(1.0f, 0.0f, 0.0f, 1.0f))

  val illegalHighlightTime: Long = 2000
  var illegalHighlightTimeSet: Long = 0
  var illegalCoords = (0, 0)

  var rotatedQuadrant: Quadrant.Value = Quadrant.first
  var rotationAngle: Int = 0

  def quadrantCentre(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-1.5f, -1.5f)
    case Quadrant.second => (1.5f, -1.5f)
    case Quadrant.third => (-1.5f, 1.5f)
    case Quadrant.fourth => (1.5f, 1.5f)
  }

  def quadrantFields(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0 to 2, 0 to 2)
    case Quadrant.second => (0 to 2, 3 to 5)
    case Quadrant.third => (3 to 5, 0 to 2)
    case Quadrant.fourth => (3 to 5, 3 to 5)
  }

  def arrowLeftPosition(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (0, -1)
    case Quadrant.second => (6, 0)
    case Quadrant.third => (-1, 5)
    case Quadrant.fourth => (5, 6)
  }

  def arrowRightPosition(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-1, 0)
    case Quadrant.second => (5, -1)
    case Quadrant.third => (0, 6)
    case Quadrant.fourth => (6, 5)
  }

  def arrowsRotation(quadrant: Quadrant.Value): Float = quadrant match {
    case Quadrant.first => 0.0f
    case Quadrant.second => 90.0f
    case Quadrant.third => 270.0f
    case Quadrant.fourth => 180.0f
  }

  private def decodeZone(x: Int, y: Int): BoardZone = {
    if (x >= 0 && x <= 5 && y >= 0 && y <= 5) {
      new FigureZone()
    } else {
      val quadrant = logicToQuadrant(x, y)
      if ((x, y) == arrowLeftPosition(quadrant)) {
        new ArrowZone(quadrant, QRotation.r90)
      } else if ((x, y) == arrowRightPosition(quadrant)) {
        new ArrowZone(quadrant, QRotation.r270)
      } else {
        new NoneZone()
      }
    }
  }

  def logicToDisplay(a: Int): Float = (2 * a - 5) / 2.0f

  def displayToLogic(a: Float): Int = {
    val i = Math.floor(Math.abs(a)).toInt
    return if (a >= 0) 3 + i else 2 - i
  }

  def logicToQuadrant(x: Int, y: Int): Quadrant.Value = {
    if (x >= 3) {
      if (y >= 3) Quadrant.fourth else Quadrant.second
    } else {
      if (y >= 3) Quadrant.third else Quadrant.first
    }
  }

  def checkIllegal(x: Int, y: Int): Unit = {
    if ((x, y) == illegalCoords) {
      if (System.currentTimeMillis() < illegalHighlightTimeSet + illegalHighlightTime) {
        colorShader.draw(rectangle, illegalHighlight)
      }
    }
  }

  def discardIllegal(): Unit = {
    illegalHighlightTimeSet -= illegalHighlightTime
  }

  def setIllegal(x: Int, y: Int): Unit = {
    illegalHighlightTimeSet = System.currentTimeMillis()
    illegalCoords = (x, y)
  }

  def drawFigure(player: Option[Player.Value], x: Int, y: Int): Unit = {
    if (player.isDefined) {
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, logicToDisplay(x), logicToDisplay(y), 0.0f)

      checkIllegal(x, y)

      if (game.finished && game.finishingMove != Nil) {
        if (game.finishingMove.contains((y, x))) {
          colorShader.draw(rectangle, winningHighlight)
        }
      }

      if (player.get == Player.O) {
        textureShader.draw(rectangle, ring)
      }
      if (player.get == Player.X) {
        textureShader.draw(rectangle, cross)
      }
      MVMatrix.pop()
    }
  }

  def drawFigures(state: game.State, quadrant: Quadrant.Value) = {
    val fields = quadrantFields(quadrant)
    for (i <- fields._1) {
      for (j <- fields._2) {
        drawFigure(state(i)(j), j, i)
      }
    }
  }

  def drawArrowPair(quadrant: Quadrant.Value, desaturated: Boolean = false) = {
    val a = arrowLeftPosition(quadrant)
    val b = arrowRightPosition(quadrant)
    val rot = arrowsRotation(quadrant)

    // Arrow Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(a._1), logicToDisplay(a._2), 0.0f)
    Matrix.rotateM(MVMatrix(), 0, rot, 0.0f, 0.0f, 1.0f)
    if (desaturated) {
      checkIllegal(a._1, a._2)
      textureShader.draw(rectangle, arrowLeftGray)
    } else {
      textureShader.draw(rectangle, arrowLeft)
    }
    MVMatrix.pop()

    // Arrow Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(b._1), logicToDisplay(b._2), 0.0f)
    Matrix.rotateM(MVMatrix(), 0, rot, 0.0f, 0.0f, 1.0f)
    if (desaturated) {
      checkIllegal(b._1, b._2)
      textureShader.draw(rectangle, arrowRightGray)
    } else {
      textureShader.draw(rectangle, arrowRight)
    }
    MVMatrix.pop()
  }

  def prepareForDisplay(): Unit = {
    Matrix.scaleM(MVMatrix(), 0, 0.25f, 0.25f, 1.0f)
  }

  def animate(): Unit = {
    if (rotationAngle > 0) {
      rotationAngle -= 2
    }

    if (rotationAngle < 0) {
      rotationAngle += 2
    }
  }

  def draw(): Unit = {
    MVMatrix.push()
    prepareForDisplay()

    val state: game.State = game.state

    for (quadrant <- Quadrant.values) {
      //Quadrants
      MVMatrix.push()
      val centre = quadrantCentre(quadrant)

      Matrix.translateM(MVMatrix(), 0, centre._1, centre._2, 0.0f)

      if (quadrant == rotatedQuadrant) {
        Matrix.rotateM(MVMatrix(), 0, rotationAngle, 0.0f, 0.0f, 1.0f)
      }

      MVMatrix.push()
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorsShader.draw(board3x3)
      MVMatrix.pop()

      Matrix.translateM(MVMatrix(), 0, -centre._1, -centre._2, 0.0f)

      drawFigures(state, quadrant)

      MVMatrix.pop()

      // Arrows
      drawArrowPair(quadrant, !game.availableRotations.contains(quadrant))
    }
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(2), logicToDisplay(7), 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 4.0f, 4.0f, 1.0f)
    playerText.draw()
    MVMatrix.pop()

    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(4), logicToDisplay(7), 0.0f)
    game.player match {
      case Player.O => textureShader.draw(rectangle, ring)
      case Player.X => textureShader.draw(rectangle, cross)
    }
    MVMatrix.pop()

    MVMatrix.pop()
  }

  def click(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean = {
    var res = true
    MVMatrix.push()
    prepareForDisplay()

    try {
      val (near, far) = unProjectMatrices(MVMatrix(), PMatrix(), clickX, clickY, viewport)
      val I = intersectRayAndXYPlane(near, far)
      processClick(I(0), I(1))
    } catch {
      case e: UnProjectException =>
        error(e.getMessage)
        res = false
      case e: IntersectException =>
        // In current scene configuration this shouldn't happen
        error(e.getMessage)
        res = false
      case e: ClickException =>
        log(e.getMessage)
        res = false
    } finally {
      MVMatrix.pop()
    }
    return res
  }

  def processClick(fx: Float, fy: Float): Unit = {
    val x = displayToLogic(fx)
    val y = displayToLogic(fy)
    try {
      decodeZone(x, y) match {
        case FigureZone() =>
          val move = new game.Position(x, y)
          game.make(move)
          discardIllegal()
        case ArrowZone(quadrant, rotation) =>
          val move = new game.Rotation(quadrant, rotation)
          game.make(move)
          discardIllegal()
          rotatedQuadrant = quadrant
          rotationAngle = rotation match {
            case QRotation.r270 => 90
            case QRotation.r90 => -90
          }
        case NoneZone() =>
          throw new ClickException("Clicked nothing")
      }
    } catch {
      case e: FieldTaken =>
        setIllegal(x, y)
      case e: RotationLocked =>
        setIllegal(x, y)
    }
  }
}

object GameBoard {
  def apply(game: Game, resources: Resources) = new GameBoard(game, resources)
}