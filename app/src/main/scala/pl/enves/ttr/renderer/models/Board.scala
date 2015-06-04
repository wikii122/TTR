package pl.enves.ttr.renderer.models

import android.opengl.Matrix
import pl.enves.ttr.renderer.{Resources, MVMatrix}

/**
 * Game board
 */
class Board(resources: Resources) {

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
    val nx = (2*x-7)/16.0f
    val ny = (2*y-7)/16.0f
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, nx, ny, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    if(figure == 0) {
      textureShader.draw(rectangle, ring)
    }else{
      textureShader.draw(rectangle, cross)
    }
    MVMatrix.pop()
  }

  def draw(): Unit = {
    MVMatrix.push()
    Matrix.scaleM(MVMatrix(), 0, 1.5f, 1.5f, 1.0f)

    //Bottom Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -3.0f/16.0f, -3.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 3.0f/8.0f, 3.0f/8.0f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Bottom Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 3.0f/16.0f, -3.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 3.0f/8.0f, 3.0f/8.0f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Top Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -3.0f/16.0f, 3.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 3.0f/8.0f, 3.0f/8.0f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Top Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 3.0f/16.0f, 3.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 3.0f/8.0f, 3.0f/8.0f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Bottom Left Arrow Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -5.0f/16.0f, -7.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    Matrix.rotateM(MVMatrix(), 0, 180.0f, 0.0f, 0.0f, 1.0f)
    textureShader.draw(rectangle, arrowLeft)
    MVMatrix.pop()

    //Bottom Left Arrow Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -7.0f/16.0f, -5.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    Matrix.rotateM(MVMatrix(), 0, 90.0f, 0.0f, 0.0f, 1.0f)
    textureShader.draw(rectangle, arrowRight)
    MVMatrix.pop()

    //Bottom Right Arrow Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 7.0f/16.0f, -5.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    Matrix.rotateM(MVMatrix(), 0, -90.0f, 0.0f, 0.0f, 1.0f)
    textureShader.draw(rectangle, arrowLeft)
    MVMatrix.pop()

    //Bottom Right Arrow Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 5.0f/16.0f, -7.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    Matrix.rotateM(MVMatrix(), 0, 180.0f, 0.0f, 0.0f, 1.0f)
    textureShader.draw(rectangle, arrowRight)
    MVMatrix.pop()

    //Top Left Arrow Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -7.0f/16.0f, 5.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    Matrix.rotateM(MVMatrix(), 0, 90.0f, 0.0f, 0.0f, 1.0f)
    textureShader.draw(rectangle, arrowLeft)
    MVMatrix.pop()

    //Top Left Arrow Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -5.0f/16.0f, 7.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    textureShader.draw(rectangle, arrowRight)
    MVMatrix.pop()

    //Top Right Arrow Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 5.0f/16.0f, 7.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    textureShader.draw(rectangle, arrowLeft)
    MVMatrix.pop()

    //Top Right Arrow Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 7.0f/16.0f, 5.0f/16.0f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 1.0f/8.0f, 1.0f/8.0f, 1.0f)
    Matrix.rotateM(MVMatrix(), 0, -90.0f, 0.0f, 0.0f, 1.0f)
    textureShader.draw(rectangle, arrowRight)
    MVMatrix.pop()

    drawFigure(0, 1, 1)
    drawFigure(1, 3, 1)
    drawFigure(0, 2, 5)
    drawFigure(1, 6, 6)

    MVMatrix.pop()
  }
}
