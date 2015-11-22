package pl.enves.ttr.logic.ai

import pl.enves.androidx.Logging
import pl.enves.ttr.logic.inner.Board
import pl.enves.ttr.logic.{Player, QRotation, Quadrant}

/**
 * Manages fields states.
 * Version without saving, safety checks, prints, and nice but time-costly things
 * With ability to reverse moves
 * TODO: Optimize more
 * TODO: Use less inner.Board
 */
class LightBoard extends Logging {
  private val quadrants = Array(
    LightBoardQuadrant(),
    LightBoardQuadrant(),
    LightBoardQuadrant(),
    LightBoardQuadrant()
  )

  private var freeFields = 36

  val _cachedState = Array.fill[Int] (6, 6) (LightField.None)

  def move(quadrant: Int, x: Int, y: Int, player: Int): Unit = {
    quadrants(quadrant).move(x, y, player)
    tick()
    freeFields -= 1
  }

  def unMove(quadrant: Int, x: Int, y: Int, player: Int): Unit = {
    freeFields += 1
    unTick()
    quadrants(quadrant).unMove(x, y, player)
  }

  def rotate(quadrant: Int, rotation: QRotation.Value, player: Int): Unit = {
    //log(s"Rotation for $quadrant by $rotation for player $player")
    quadrants(quadrant).rotate(rotation)
    tick()
  }

  def unRotate(quadrant: Int, rotation: QRotation.Value, player: Int): Unit = {
    unTick()
    quadrants(quadrant).unRotate(rotation)
  }

  def tick(): Unit = {
    quadrants(0).tickCooldown()
    quadrants(1).tickCooldown()
    quadrants(2).tickCooldown()
    quadrants(3).tickCooldown()
  }

  def unTick(): Unit = {
    quadrants(0).unTickCooldown()
    quadrants(1).unTickCooldown()
    quadrants(2).unTickCooldown()
    quadrants(3).unTickCooldown()
  }

  def finished = freeFields == 0

  def state(): Array[Array[Int]] = {
    var x = 0
    var y = 0
    while (x < Quadrant.size) {
      y = 0
      while (y < Quadrant.size) {
        _cachedState(x)(y) = quadrants(0).get(x, y)
        _cachedState(x+Quadrant.size)(y) = quadrants(1).get(x, y)
        _cachedState(x)(y+Quadrant.size) = quadrants(2).get(x, y)
        _cachedState(x+Quadrant.size)(y+Quadrant.size) = quadrants(3).get(x, y)
        y+=1
      }
      x+=1
    }
    return _cachedState
  }

  def quadrantField(quadrant: Int, x: Int, y: Int) = quadrants(quadrant).get(x, y)

  def canRotate(quadrant: Int) = quadrants(quadrant).canRotate

  def getFreeFields = freeFields

  def getQuadrant(quadrant: Int) = quadrants(quadrant)
}

object LightBoard {
  def apply() = new LightBoard()
  def apply(old: Board): LightBoard = {
    val board = new LightBoard()
    board.freeFields = old.getFreeFields

    for(q <- Quadrant.values) {
      val oldq = old.getQuadrant(q)
      val newq = board.quadrants(q.id)
      newq.setRotation(oldq.getRotation.id)
      newq.setCooldown(oldq.getCooldown)
      for (i <- 0 until Quadrant.size;
           j <- 0 until Quadrant.size
      ) newq.fields(i)(j) = LightField.fromOption(oldq.fields(i)(j))
    }

    return board
  }
}
