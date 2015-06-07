package pl.enves.ttr.graphics

import javax.microedition.khronos.opengles.GL10

import android.opengl.{GLU, Matrix}
import pl.enves.ttr.graphics.DrawReason.DrawReason
import pl.enves.ttr.utils.{Logging, Vector3}

/**
 * Game board
 */
class GameBoard(resources: Resources) extends Logging {

  val board3x3 = resources.getGeometry(resources.ModelId.Board3x3)
  val rectangle = resources.getGeometry(resources.ModelId.Rectangle)

  val arrowLeft = resources.getTexture(resources.TextureId.ArrowLeft)
  val arrowRight = resources.getTexture(resources.TextureId.ArrowRight)
  val ring = resources.getTexture(resources.TextureId.Ring)
  val cross = resources.getTexture(resources.TextureId.Cross)

  val colorShader = resources.getShader(resources.ShaderId.Color)
  val textureShader = resources.getShader(resources.ShaderId.Texture)

  def animate(dt: Float = 0.0f): Unit = ???

  //TODO: Correct after merge with Logic
  def drawFigure(figure: Int, x: Int, y: Int): Unit = {
    val nx = (2 * x - 7) / 2.0f
    val ny = (2 * y - 7) / 2.0f
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, nx, ny, 0.0f)
    if (figure == 0) {
      textureShader.draw(rectangle, ring)
    } else {
      textureShader.draw(rectangle, cross)
    }
    MVMatrix.pop()
  }

  def draw(drawReason: DrawReason): Unit = {
    MVMatrix.push()
    Matrix.scaleM(MVMatrix(), 0, 3.0f / 16.0f, 3.0f / 16.0f, 1.0f)

    if (drawReason == DrawReason.Render) {
      //Bottom Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -3.0f / 2, -3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorShader.draw(board3x3)
      MVMatrix.pop()

      //Bottom Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 3.0f / 2, -3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorShader.draw(board3x3)
      MVMatrix.pop()

      //Top Left
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, -3.0f / 2, 3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorShader.draw(board3x3)
      MVMatrix.pop()

      //Top Right
      MVMatrix.push()
      Matrix.translateM(MVMatrix(), 0, 3.0f / 2, 3.0f / 2, 0.0f)
      Matrix.scaleM(MVMatrix(), 0, 3.0f, 3.0f, 1.0f)
      colorShader.draw(board3x3)
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

      drawFigure(0, 1, 1)
      drawFigure(1, 3, 1)
      drawFigure(0, 2, 5)
      drawFigure(1, 6, 6)
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
          log("intersect:  " + I(0) + " " + I(1) + " " + I(2))
          val x = I(0)
          val y = I(1)

          val iax = Math.floor(Math.abs(x)).toInt
          val iay = Math.floor(Math.abs(y)).toInt

          if (iax <= 2 && iay <= 2) {
            log("Clicked board: " + iax + " " + iay)
          } else if (iax == 2 && iay == 3) {
            log("Clicked left arrow")
          } else if (iax == 3 && iay == 2) {
            log("Clicked right arrow")
          } else {
            log("Clicked nothing")
          }

          if (x >= 0) {
            if (y >= 0) {
              log("In the upper right corner")
            } else {
              log("In the down right corner")
            }
          } else {
            if (y >= 0) {
              log("In the upper left corner")
            } else {
              log("In the down left corner")
            }
          }
        }
      } else {
        error("ModelView or Projection Matrix cannot be inverted")
      }
    }

    MVMatrix.pop()
  }

  def intersectRayAndXYPlane(P0: Array[Float], P1: Array[Float], I: Array[Float]): Boolean = {
    val planePoint = Array(0.0f, 0.0f, 0.0f)
    val planeNormal = Array(0.0f, 0.0f, 1.0f)

    val rayDirection, w0, temp = new Array[Float](3)
    var r, a, b: Float = 0.0f

    val SMALL_NUM = 0.0001f

    Vector3.sub(rayDirection, P1, P0)
    Vector3.sub(w0, P0, planePoint)
    a = -Vector3.dotProduct(planeNormal, w0)
    b = Vector3.dotProduct(planeNormal, rayDirection)
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
    Vector3.scale(temp, r, rayDirection)
    Vector3.add(I, P0, temp)

    return true
  }
}

object GameBoard {
  def apply(resources: Resources) = new GameBoard(resources)
}