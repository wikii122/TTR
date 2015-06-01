package pl.enves.ttr.renderer

import android.opengl.Matrix

import scala.collection.mutable

//TODO: better
object MVMatrix {
  var matrix = new Array[Float](16)
  Matrix.setIdentityM(matrix, 0)
  val buf = new mutable.Stack[Array[Float]]

  def apply(): Array[Float] = matrix

  def push(): Unit =
    buf.push(matrix.clone())

  def pop(): Unit =
    matrix = buf.pop()

  def clear(): Unit = {
    Matrix.setIdentityM(matrix, 0)
    buf.clear()
  }
}

//TODO: better
object PMatrix {
  var matrix = new Array[Float](16)
  Matrix.setIdentityM(matrix, 0)

  def apply(): Array[Float] = matrix
}
