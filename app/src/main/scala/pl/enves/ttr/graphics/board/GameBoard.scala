package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Size: Outer: 2.0x(2.0~2.5), inner: 8.0x(8.0~10.0)
 * (0.0, 0.0) is in the middle
 */
class GameBoard(game: Game, resources: Resources) extends SceneObject with Logging with Algebra {

  private[this] val currentPlayerIndicator = new CurrentPlayerIndicator(game, resources)
  addChild(currentPlayerIndicator)

  private[this] val winnerIndicator = new WinnerIndicator(game, resources)
  addChild(winnerIndicator)

  private[this] val quadrants = Map(
    (Quadrant.first, new GameQuadrant(game, Quadrant.first, resources)),
    (Quadrant.second, new GameQuadrant(game, Quadrant.second, resources)),
    (Quadrant.third, new GameQuadrant(game, Quadrant.third, resources)),
    (Quadrant.fourth, new GameQuadrant(game, Quadrant.fourth, resources))
  )

  private[this] val allArrows = Array(
    (Quadrant.first, QRotation.r90),
    (Quadrant.first, QRotation.r270),
    (Quadrant.second, QRotation.r90),
    (Quadrant.second, QRotation.r270),
    (Quadrant.third, QRotation.r90),
    (Quadrant.third, QRotation.r270),
    (Quadrant.fourth, QRotation.r90),
    (Quadrant.fourth, QRotation.r270)
  )

  private[this] val arrows = allArrows map { key => key -> new Arrow(game, key._1, key._2, resources) } toMap

  for ((name, arrow) <- arrows) {
    addChild(arrow)
  }

  for (quadrant <- Quadrant.values) {
    addChild(quadrants(quadrant))
  }

  private[this] val replayIndicator = new ReplayIndicator(game, resources)
  addChild(replayIndicator)

  override def onUpdateResources(screenRatio: Float): Unit = {
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

    replayIndicator.addTranslation(0.0f, 0.0f, 0.5f, true)
    replayIndicator.addRotation(45.0f, 0.0f, 0.0f, 1.0f, true)
    replayIndicator.addScale(12.0f, 12.0f, 1.0f, true)

    for ((name, arrow) <- arrows) {
      val pos = name._2 match {
        case QRotation.r90 => arrowLeftPosition(name._1)
        case QRotation.r270 => arrowRightPosition(name._1)
      }
      arrow.addTranslation(pos._1, pos._2, 0.0f, true)
    }

    for (quadrant <- Quadrant.values) {
      val centre = quadrantCentre(quadrant)
      quadrants(quadrant).addTranslation(centre._1, centre._2, 0.0f, true)
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
    val availableRotations = game.availableRotations
    for ((name, arrow) <- arrows) {
      arrow.setActive(availableRotations.contains(name._1))
    }
  }
}
