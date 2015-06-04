package pl.enves.ttr.renderer

import pl.enves.ttr.renderer.models.Board

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