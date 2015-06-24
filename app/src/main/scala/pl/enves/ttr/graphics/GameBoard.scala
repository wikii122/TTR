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
  val ring = new TextureShaderData(resources.getTexture(resources.TextureId.Ring))
  val cross = new TextureShaderData(resources.getTexture(resources.TextureId.Cross))

  val colorShader = resources.getShader(resources.ShaderId.Color)
  val colorsShader = resources.getShader(resources.ShaderId.Colors)
  val textureShader = resources.getShader(resources.ShaderId.Texture)

  val highlightTime:Long = 2000
  var highlightTimeSet: Long = 0
  var highlightX = 0
  var highlightY = 0

  def animate(dt: Float = 0.0f): Unit = ???

  def translate(a: Int): Float = (2 * a - 5) / 2.0f

  def drawFigure(player: Option[Player.Value] , x: Int, y: Int): Unit = {
    if(player.isDefined) {
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, translate(x), translate(y), 0.0f)
      if (player.get == Player.O) {
        textureShader.draw(rectangle, ring)
      }
      if (player.get == Player.X) {
        textureShader.draw(rectangle, cross)
      }
      MVMatrix.pop()
    }
  }

  def draw(drawReason: DrawReason): Boolean = {
    var res = true

    MVMatrix.push()
    Matrix.scaleM(MVMatrix(), 0, 0.25f, 0.25f, 1.0f)

    if (drawReason == DrawReason.Render) {
      //Highlight
      if(System.currentTimeMillis() < highlightTimeSet + highlightTime) {
        MVMatrix.push()
        Matrix.translateM(MVMatrix(), 0, translate(highlightX), translate(highlightY), 0.0f)
        colorShader.draw(rectangle, new ColorShaderData(Array(1.0f, 0.0f, 0.0f, 1.0f)))
        MVMatrix.pop()
      }

      //Bottom Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -3.0f / 2, -3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorsShader.draw(board3x3)
      MVMatrix.pop()

      //Bottom Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 3.0f / 2, -3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorsShader.draw(board3x3)
      MVMatrix.pop()

      //Top Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -3.0f / 2, 3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorsShader.draw(board3x3)
      MVMatrix.pop()

      //Top Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 3.0f / 2, 3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorsShader.draw(board3x3)
      MVMatrix.pop()

      //Bottom Left Arrow Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -5.0f / 2, -7.0f / 2, 0.0f)
      Matrix.rotateM(MVMatrix(), 0, 180.0f, 0.0f, 0.0f, 1.0f)
      textureShader.draw(rectangle, arrowLeft)
      MVMatrix.pop()

      //Bottom Left Arrow Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -7.0f / 2, -5.0f / 2, 0.0f)
      Matrix.rotateM(MVMatrix(), 0, 90.0f, 0.0f, 0.0f, 1.0f)
      textureShader.draw(rectangle, arrowRight)
      MVMatrix.pop()

      //Bottom Right Arrow Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 7.0f / 2, -5.0f / 2, 0.0f)
      Matrix.rotateM(MVMatrix(), 0, -90.0f, 0.0f, 0.0f, 1.0f)
      textureShader.draw(rectangle, arrowLeft)
      MVMatrix.pop()

      //Bottom Right Arrow Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 5.0f / 2, -7.0f / 2, 0.0f)
      Matrix.rotateM(MVMatrix(), 0, 180.0f, 0.0f, 0.0f, 1.0f)
      textureShader.draw(rectangle, arrowRight)
      MVMatrix.pop()

      //Top Left Arrow Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -7.0f / 2, 5.0f / 2, 0.0f)
      Matrix.rotateM(MVMatrix(), 0, 90.0f, 0.0f, 0.0f, 1.0f)
      textureShader.draw(rectangle, arrowLeft)
      MVMatrix.pop()

      //Top Left Arrow Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -5.0f / 2, 7.0f / 2, 0.0f)
      textureShader.draw(rectangle, arrowRight)
      MVMatrix.pop()

      //Top Right Arrow Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 5.0f / 2, 7.0f / 2, 0.0f)
      textureShader.draw(rectangle, arrowLeft)
      MVMatrix.pop()

      //Top Right Arrow Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 7.0f / 2, 5.0f / 2, 0.0f)
      Matrix.rotateM(MVMatrix(), 0, -90.0f, 0.0f, 0.0f, 1.0f)
      textureShader.draw(rectangle, arrowRight)
      MVMatrix.pop()

      val state: game.State = game.state
      for(i <- 0 to 5) {
        for(j <- 0 to 5) {
          drawFigure(state(i)(j), j, i)
        }
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

          val iax = Math.floor(Math.abs(x)).toInt
          val iay = Math.floor(Math.abs(y)).toInt

          var quadrant: Quadrant.Value = Quadrant.first
          var arrowsReversed = false
          if (x >= 0) {
            if (y >= 0) {
              quadrant = Quadrant.fourth
            } else {
              quadrant = Quadrant.second
              arrowsReversed = true
            }
          } else {
            if (y >= 0) {
              quadrant = Quadrant.third
              arrowsReversed = true
            } else {
              quadrant = Quadrant.first
            }
          }

          if (iax <= 2 && iay <= 2) {
            val a = if(x>=0) 3+iax else 2-iax
            val b = if(y>=0) 3+iay else 2-iay
            val position = new game.Position(a, b)
            try {
              game.make(position)
            }catch {
              case e: FieldTaken => {
                highlightTimeSet = System.currentTimeMillis()
                highlightX = a
                highlightY = b
              }
            }
          } else if (iax == 2 && iay == 3) {
            val rot = if(arrowsReversed) QRotation.r270 else QRotation.r90
            val rotation = new game.Rotation(quadrant, rot)
            game.make(rotation)
          } else if (iax == 3 && iay == 2) {
            val rot = if(arrowsReversed) QRotation.r90 else QRotation.r270
            val rotation = new game.Rotation(quadrant, rot)
            game.make(rotation)
          } else {
            log("Clicked nothing")
            res = false
          }
        }
      } else {
        error("ModelView or Projection Matrix cannot be inverted")
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