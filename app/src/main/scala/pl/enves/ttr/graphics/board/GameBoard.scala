package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Size: Outer: 2.0x(2.0~2.5), inner: 8.0x(8.0~10.0)
 * (0.0, 0.0) is in the middle
 */
class GameBoard(game: Game) extends SceneObject with Logging with Algebra {

  private[this] val currentPlayerIndicator = new CurrentPlayerIndicator(game)
  addChild(currentPlayerIndicator)

  private[this] val winnerIndicator = new WinnerIndicator(game)
  addChild(winnerIndicator)

  private[this] val quadrants = Array(
    new GameQuadrant(game, Quadrant.first),
    new GameQuadrant(game, Quadrant.second),
    new GameQuadrant(game, Quadrant.third),
    new GameQuadrant(game, Quadrant.fourth)
  )

  private[this] val arrows = Array(
    createArrowPair(Quadrant.first),
    createArrowPair(Quadrant.second),
    createArrowPair(Quadrant.third),
    createArrowPair(Quadrant.fourth)
  )

  for (quadrant <- Quadrant.values) {
    addChild(quadrants(quadrant.id))
  }

  for (quadrant <- Quadrant.values) {
    val pair = arrows(quadrant.id)
    addChild(pair._1)
    addChild(pair._2)
  }

  private def createArrowPair(quadrant: Quadrant.Value) = (
    new Arrow(game, quadrant, QRotation.r90),
    new Arrow(game, quadrant, QRotation.r270)
    )

  override def onUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    addScale(0.25f, 0.25f, 1.0f, true)

    //screenRatio should be >= 1, as is ensured in GameRenderer
    val halfScreenHeight = 4.0f * screenRatio
    //board is square, takes whole width
    val heightLeft = halfScreenHeight - 4.0f
    //fit indicator between arrows when there is little space left
    var indicatorPositionY = 3.5f + heightLeft / 2
    //but do not allow it to be too distant
    if (indicatorPositionY > 4.5f) {
      indicatorPositionY = 4.5f
    }

    currentPlayerIndicator.addTranslation(0.0f, indicatorPositionY, 0.0f, true)
    currentPlayerIndicator.addScale(4.0f, 4.0f, 1.0f, true)

    winnerIndicator.addTranslation(0.0f, -indicatorPositionY, 0.0f, true)
    winnerIndicator.addScale(4.0f, 4.0f, 1.0f, true)

    for (quadrant <- Quadrant.values) {
      val pair = arrows(quadrant.id)

      val posRight = arrowRightPosition(quadrant)
      pair._1.addTranslation(posRight._1, posRight._2, 0.0f, true)

      val posLeft = arrowLeftPosition(quadrant)
      pair._2.addTranslation(posLeft._1, posLeft._2, 0.0f, true)
    }

    for (quadrant <- Quadrant.values) {
      val centre = quadrantCentre(quadrant)
      quadrants(quadrant.id).addTranslation(centre._1, centre._2, 0.0f, true)
    }
  }

  private def quadrantCentre(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-1.5f, -1.5f)
    case Quadrant.second => (1.5f, -1.5f)
    case Quadrant.third => (-1.5f, 1.5f)
    case Quadrant.fourth => (1.5f, 1.5f)
  }

  private def arrowRightPosition(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-2.5f, -3.5f)
    case Quadrant.second => (3.5f, -2.5f)
    case Quadrant.third => (-3.5f, 2.5f)
    case Quadrant.fourth => (2.5f, 3.5f)
  }

  private def arrowLeftPosition(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-3.5f, -2.5f)
    case Quadrant.second => (2.5f, -3.5f)
    case Quadrant.third => (-2.5f, 3.5f)
    case Quadrant.fourth => (3.5f, 2.5f)
  }

  override def onAnimate(dt: Float): Unit = {
    var i = 0
    while (i < 4) {
      val quadrant = Quadrant(i)
      val pair = arrows(quadrant.id)
      val active = game.canRotate(quadrant)
      pair._1.setActive(active)
      pair._2.setActive(active)
      i += 1
    }


    if (game.finished && game.finishingMove != Nil) {
      for ((x, y) <- game.finishingMove) {
        val quadrant = if (y < Quadrant.size) {
          if (x < Quadrant.size) Quadrant.first
          else Quadrant.second
        } else {
          if (x < Quadrant.size) Quadrant.third
          else Quadrant.fourth
        }

        quadrants(quadrant.id).setWinning(x % Quadrant.size, y % Quadrant.size)
      }
    }
  }
}
