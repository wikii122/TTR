package pl.enves.ttr.graphics.board

import android.opengl.Matrix
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Game board
 * Uses 2 coordinate systems:
 * Logic: (Int, Int), (0, 0) is where (0, 0) field is
 * Display: (Float, Float), (0, 0) is in the center of displayed board
 */
class GameBoard(game: Game, resources: Resources) extends SceneObject with Logging with Algebra with Coordinates {

  private class ClickException(msg: String) extends RuntimeException(msg)

  private abstract class BoardZone()

  private case class NoneZone() extends BoardZone()

  private case class FigureZone() extends BoardZone()

  private case class ArrowZone(quadrant: Quadrant.Value, rotation: QRotation.Value) extends BoardZone()

  val currentPlayerIndicator = new CurrentPlayerIndicator(game, resources)
  currentPlayerIndicator.objectPosition =  Array(0.0f, logicToDisplay(7), 0.0f)
  currentPlayerIndicator.objectScale = Array(4.0f, 4.0f, 1.0f)
  addChild(currentPlayerIndicator)

  var board3x3: Option[Geometry] = None
  var rectangle: Option[Geometry] = None

  var arrowLeft: Option[Int] = None
  var arrowRight: Option[Int] = None

  var ring: Option[Int] = None
  var cross: Option[Int] = None
  var empty: Option[Int] = None

  var colorShader: Option[ColorShader] = None
  var colorsShader: Option[ColorsShader] = None
  var textureShader: Option[TextureShader] = None
  var maskShader: Option[MaskShader] = None

  val winningHighlight = (0.0f, 1.0f, 0.0f, 1.0f)
  val illegalHighlight = (1.0f, 0.0f, 0.0f, 1.0f)

  //TODO: Load from settings
  var crossColor = Array(27.0f/255.0f, 20.0f/255.0f, 100.0f/255.0f, 1.0f)
  var ringColor = Array(27.0f/255.0f, 20.0f/255.0f, 100.0f/255.0f, 1.0f)
  var outerColor1 = Array(179.0f/255.0f, 179.0f/255.0f, 179.0f/255.0f, 1.0f)
  var outerColor2 = Array(255.0f/255.0f, 255.0f/255.0f, 255.0f/255.0f, 1.0f)
  var winnerOuterColor = Array(0.0f/255.0f, 179.0f/255.0f, 0.0f/255.0f, 1.0f)
  var illegalOuterColor = Array(179.0f/255.0f, 0.0f/255.0f, 0.0f/255.0f, 1.0f)
  var inactiveColor = Array(55.0f/255.0f, 55.0f/255.0f, 55.0f/255.0f, 1.0f)
  var backgroundColor = Array(27.0f/255.0f, 20.0f/255.0f, 100.0f/255.0f, 1.0f)


  val noColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  val illegalHighlightTime: Long = 2000
  var illegalHighlightTimeSet: Long = 0
  var illegalCoords = (0, 0)

  var rotatedQuadrant: Quadrant.Value = Quadrant.first
  var quadrantRotationAngle: Int = 0

  objectScale = Array(0.25f, 0.25f, 1.0f)

  override def onUpdateResources(): Unit = {
    log("onUpdateResources")

    rectangle = Some(resources.getGeometry(DefaultGeometryId.Square.toString))

    arrowLeft = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskArrowLeft.toString))
    arrowRight = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskArrowRight.toString))

    ring = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskRing.toString))
    cross = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskCross.toString))
    empty = Some(resources.getTexture(DefaultTextureId.Pat1x1MaskEmpty.toString))

    colorShader = Some(resources.getShader(ShaderId.Color).asInstanceOf[ColorShader])
    colorsShader = Some(resources.getShader(ShaderId.Colors).asInstanceOf[ColorsShader])
    textureShader = Some(resources.getShader(ShaderId.Texture).asInstanceOf[TextureShader])
    maskShader = Some(resources.getShader(ShaderId.Mask).asInstanceOf[MaskShader])
  }

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

  def defaultOuterColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => outerColor1
    case Quadrant.second => outerColor2
    case Quadrant.third => outerColor2
    case Quadrant.fourth => outerColor1
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

  def logicToQuadrant(x: Int, y: Int): Quadrant.Value = {
    if (x >= 3) {
      if (y >= 3) Quadrant.fourth else Quadrant.second
    } else {
      if (y >= 3) Quadrant.third else Quadrant.first
    }
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

  def checkWinning(x: Int, y:Int): Boolean = {
    game.finished && game.finishingMove != Nil && game.finishingMove.contains((y, x))
  }

  def drawFigure(player: Option[Player.Value], x: Int, y: Int, quadrant: Quadrant.Value): Unit = {
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(x), logicToDisplay(y), 0.0f)

    val outer = if(checkIllegal(x, y)) {
      illegalOuterColor
    } else {
      if(checkWinning(x, y)) {
        winnerOuterColor
      } else {
        defaultOuterColor(quadrant)
      }
    }

    if (player.isDefined) {
      if (player.get == Player.O) {
        maskShader.get.draw(rectangle.get, (noColor, ringColor, outer, ring.get))
      }
      if (player.get == Player.X) {
        maskShader.get.draw(rectangle.get, (noColor, crossColor, outer, cross.get))
      }
    } else {
      maskShader.get.draw(rectangle.get, (noColor, noColor, outer, empty.get))
    }

    MVMatrix.pop()
  }

  def drawFigures(state: game.State, quadrant: Quadrant.Value) = {
    val fields = quadrantFields(quadrant)
    for (i <- fields._1) {
      for (j <- fields._2) {
        drawFigure(state(i)(j), j, i, quadrant)
      }
    }
  }

  def drawArrowPair(quadrant: Quadrant.Value, desaturated: Boolean = false) = {
    val a = arrowLeftPosition(quadrant)
    val b = arrowRightPosition(quadrant)
    val rot = arrowsRotation(quadrant)

    val inner = if(desaturated) {
      inactiveColor
    } else {
      defaultOuterColor(quadrant)
    }

    val outer1 = if(checkIllegal(a._1, a._2)) {
      illegalOuterColor
    }else {
      noColor
    }

    val outer2 = if(checkIllegal(b._1, b._2)) {
      illegalOuterColor
    }else {
      noColor
    }

    // Arrow Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(a._1), logicToDisplay(a._2), 0.0f)
    Matrix.rotateM(MVMatrix(), 0, rot, 0.0f, 0.0f, 1.0f)
    maskShader.get.draw(rectangle.get, (noColor, inner, outer1, arrowLeft.get))
    MVMatrix.pop()

    // Arrow Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, logicToDisplay(b._1), logicToDisplay(b._2), 0.0f)
    Matrix.rotateM(MVMatrix(), 0, rot, 0.0f, 0.0f, 1.0f)
    maskShader.get.draw(rectangle.get, (noColor, inner, outer2, arrowRight.get))
    MVMatrix.pop()
  }

  override def onAnimate(dt: Float): Unit = {
    if (quadrantRotationAngle > 0) {
      quadrantRotationAngle -= 2
    }

    if (quadrantRotationAngle < 0) {
      quadrantRotationAngle += 2
    }
  }

  override def onDraw(): Unit = {
    val state: game.State = game.state

    for (quadrant <- Quadrant.values) {
      //Quadrants
      MVMatrix.push()
      val centre = quadrantCentre(quadrant)

      Matrix.translateM(MVMatrix(), 0, centre._1, centre._2, 0.0f)

      if (quadrant == rotatedQuadrant) {
        Matrix.rotateM(MVMatrix(), 0, quadrantRotationAngle, 0.0f, 0.0f, 1.0f)
      }

      Matrix.translateM(MVMatrix(), 0, -centre._1, -centre._2, 0.0f)

      drawFigures(state, quadrant)

      MVMatrix.pop()

      // Arrows
      drawArrowPair(quadrant, !game.availableRotations.contains(quadrant))
    }
  }

  override def onClick(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean = {
    var res = true

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
          quadrantRotationAngle = rotation match {
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