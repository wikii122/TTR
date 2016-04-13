package pl.enves.ttr.graphics

import android.opengl.Matrix
import pl.enves.androidx.Logging

class MatrixStack(size: Int = 1) extends Logging {
  private[this] val stack = new Array[Array[Float]](size)
  private[this] var pointer: Int = 0

  for (i <- 0 to size - 1) {
    stack(i) = new Array[Float](16)
  }

  clear()

  def get(): Array[Float] = stack(pointer)

  def push(): Unit =
    if (pointer + 1 < size) {
      val oldMatrix = stack(pointer)
      val newMatrix = stack(pointer + 1)
      var i = 0
      while (i < 16) {
        newMatrix(i) = oldMatrix(i)
        i += 1
      }
      pointer += 1
    } else {
      error("Stack full, cannot push")
    }

  def pop(): Unit =
    if (pointer > 0) {
      pointer -= 1
    } else {
      error("Stack empty, cannot pop")
    }

  def clear(): Unit = {
    pointer = 0
    Matrix.setIdentityM(stack(pointer), 0)
  }
}

