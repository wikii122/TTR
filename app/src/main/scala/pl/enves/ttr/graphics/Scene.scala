package pl.enves.ttr.graphics

import pl.enves.ttr.graphics.models.Board

class Scene(resources: Resources) {
  val board = new Board(resources)

  def animate(): Unit = {

  }

  def draw() {
    board.draw()
  }
}

object Scene {
  def apply(resources: Resources) = new Scene(resources)
}