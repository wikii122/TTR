package pl.enves.ttr.graphics

import android.opengl.Matrix

import scala.collection.mutable

//TODO: better
class MatrixStack {
  var matrix = new Array[Float](16)
  Matrix.setIdentityM(matrix, 0)
  val buf = new mutable.Stack[Array[Float]]

  def get(): Array[Float] = matrix

  def push(): Unit =
    buf.push(matrix.clone())

  def pop(): Unit =
    matrix = buf.pop()

  def clear(): Unit = {
    Matrix.setIdentityM(matrix, 0)
    buf.clear()
  }
}

