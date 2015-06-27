package pl.enves.ttr.graphics

import javax.microedition.khronos.opengles.GL10

import android.opengl.{GLU, Matrix}
import pl.enves.ttr.graphics.DrawReason.DrawReason
import pl.enves.ttr.graphics.shaders.{ColorShaderData, TextureShaderData}
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.{Logging, Vector3}

/**
 * Game board
 */
class GameBoard(game: Game, resources: Resources) extends Logging with Vector3 {

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

  def toCenter(a: Int): Float = (2 * a - 5) / 2.0f

  def toLogical(a: Float): Int = {
    val i = Math.floor(Math.abs(a)).toInt
    return if (a >= 0) 3 + i else 2 - i
  }

  def clickQuadrant(x: Float, y: Float): Quadrant.Value = {
    if (x >= 0) {
      if (y >= 0) Quadrant.fourth else Quadrant.second
    } else {
      if (y >= 0) Quadrant.third else Quadrant.first
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

  def drawFigure(player: Option[Player.Value], x: Int, y: Int): Unit = {
    if (player.isDefined) {
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, toCenter(x), toCenter(y), 0.0f)

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
    Matrix.translateM(MVMatrix(), 0, toCenter(a._1), toCenter(a._2), 0.0f)
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
    Matrix.translateM(MVMatrix(), 0, toCenter(b._1), toCenter(b._2), 0.0f)
    Matrix.rotateM(MVMatrix(), 0, rot, 0.0f, 0.0f, 1.0f)
    if (desaturated) {
      checkIllegal(b._1, b._2)
      textureShader.draw(rectangle, arrowRightGray)
    } else {
      textureShader.draw(rectangle, arrowRight)
    }
    MVMatrix.pop()
  }

  def draw(drawReason: DrawReason): Boolean = {
    var res = true

    MVMatrix.push()
    Matrix.scaleM(MVMatrix(), 0, 0.25f, 0.25f, 1.0f)

    if (drawReason == DrawReason.Render) {

      if (rotationAngle > 0) {
        rotationAngle -= 2
      }

      if (rotationAngle < 0) {
        rotationAngle += 2
      }

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
    }

    if (drawReason == DrawReason.Click) {
      val temp1 = new Array[Float](4)
      val temp2 = new Array[Float](4)
      val near = new Array[Float](3)
      val far = new Array[Float](3)

      val result1 = GLU.gluUnProject(ClickInfo.X, ClickInfo.Y, 1.0f, MVMatrix(), 0, PMatrix(), 0, ClickInfo.viewport, 0, temp1, 0)
      val result2 = GLU.gluUnProject(ClickInfo.X, ClickInfo.Y, 0.0f, MVMatrix(), 0, PMatrix(), 0, ClickInfo.viewport, 0, temp2, 0)

      if (result1 == GL10.GL_TRUE && result2 == GL10.GL_TRUE) {
        near(0) = temp1(0) / temp1(3)
        near(1) = temp1(1) / temp1(3)
        near(2) = temp1(2) / temp1(3)

        far(0) = temp2(0) / temp2(3)
        far(1) = temp2(1) / temp2(3)
        far(2) = temp2(2) / temp2(3)

        val I = new Array[Float](3)
        if (intersectRayAndXYPlane(near, far, I)) {
          val x = I(0)
          val y = I(1)

          val lx = toLogical(x)
          val ly = toLogical(y)

          if (lx >= 0 && lx <= 5 && ly >= 0 && ly <= 5) {
            val position = new game.Position(lx, ly)
            try {
              game.make(position)
              discardIllegal()
            } catch {
              case e: FieldTaken =>
                illegalHighlightTimeSet = System.currentTimeMillis()
                illegalCoords = (lx, ly)
            }
          } else {
            val quadrant = clickQuadrant(x, y)

            val rot = if ((lx, ly) == arrowLeftPosition(quadrant)) {
              QRotation.r90
            } else if ((lx, ly) == arrowRightPosition(quadrant)) {
              QRotation.r270
            } else {
              null
            }

            if (rot != null) {
              val rotation = new game.Rotation(quadrant, rot)
              try {
                rotatedQuadrant = quadrant
                rotationAngle = rot match {
                  case QRotation.r270 => 90
                  case QRotation.r90 => -90
                }
                game.make(rotation)
                discardIllegal()
              } catch {
                case e: RotationLocked =>
                  rotationAngle = 0
                  illegalHighlightTimeSet = System.currentTimeMillis()
                  illegalCoords = (lx, ly)
              }
            } else {
              log("Clicked nothing")
              res = false
            }
          }
        } else {
          error("ModelView or Projection Matrix cannot be inverted")
        }
      }
    }
    MVMatrix.pop()

    return res
  }

  def intersectRayAndXYPlane(P0: Array[Float], P1: Array[Float], I: Array[Float]): Boolean = {
    val planePoint = Array(0.0f, 0.0f, 0.0f)
    val planeNormal = Array(0.0f, 0.0f, 1.0f)

    var r, a, b: Float = 0.0f

    val SMALL_NUM = 0.0001f

    val rayDirection = sub(P1, P0)
    val w0 = sub(P0, planePoint)
    a = -dotProduct(planeNormal, w0)
    b = dotProduct(planeNormal, rayDirection)
    if (Math.abs(b) < SMALL_NUM) {
      // ray is parallel to plane
      //if (a == 0) {
      // ray lies in plane
      //}
      return false
    }

    // Check if specified segment intersects with plane
    r = a / b
    if (r < 0.0f || r > 1.0f) {
      return false
    }

    // Get intersection point
    val temp = scale(r, rayDirection)
    val tI = add(P0, temp)

    tI.indices foreach {
      i => I(i) = tI(i)
    }

    return true
  }
}

object GameBoard {
  def apply(game: Game, resources: Resources) = new GameBoard(game, resources)
}