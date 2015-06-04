package pl.enves.ttr.renderer.models

import android.opengl.Matrix
import pl.enves.ttr.renderer.{Resources, MVMatrix}

/**
 * Game board
 */
class Board(resources: Resources) {

  val board3x3 = resources.getGeometry(resources.ModelId.Board3x3)
  val colorShader = resources.getShader(resources.ShaderId.Color)

  def animate(dt: Float = 0.0f): Unit = ???

  def draw(): Unit = {
    MVMatrix.push()
    Matrix.scaleM(MVMatrix(), 0, 1.5f, 1.5f, 1.0f)

    //Bottom Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.25f, -0.25f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 0.5f, 0.5f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Bottom Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.25f, -0.25f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 0.5f, 0.5f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Top Left
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, -0.25f, 0.25f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 0.5f, 0.5f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()

    //Top Right
    MVMatrix.push()
    Matrix.translateM(MVMatrix(), 0, 0.25f, 0.25f, 0.0f)
    Matrix.scaleM(MVMatrix(), 0, 0.5f, 0.5f, 1.0f)
    colorShader.draw(board3x3)
    MVMatrix.pop()
    MVMatrix.pop()
  }
}
