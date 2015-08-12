package pl.enves.ttr.graphics.board

import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Size: Outer: ~2.0x~2.0, inner: ~8.0x~8.0
 * (0.0, 0.0) is in the middle
 */
class GameBoard(game: Game, resources: Resources) extends SceneObject with Logging with Algebra {

  val currentPlayerIndicator = new CurrentPlayerIndicator(game, resources)
  currentPlayerIndicator.translate(0.0f, 4.5f, 0.0f)
  currentPlayerIndicator.scale(4.0f, 4.0f, 1.0f)
  addChild(currentPlayerIndicator)

  val quadrants = Map(
    (Quadrant.first, new GameQuadrant(game, Quadrant.first, resources)),
    (Quadrant.second, new GameQuadrant(game, Quadrant.second, resources)),
    (Quadrant.third, new GameQuadrant(game, Quadrant.third, resources)),
    (Quadrant.fourth, new GameQuadrant(game, Quadrant.fourth, resources))
  )

  val allArrows = Array(
    (Quadrant.first, QRotation.r90),
    (Quadrant.first, QRotation.r270),
    (Quadrant.second, QRotation.r90),
    (Quadrant.second, QRotation.r270),
    (Quadrant.third, QRotation.r90),
    (Quadrant.third, QRotation.r270),
    (Quadrant.fourth, QRotation.r90),
    (Quadrant.fourth, QRotation.r270)
  )

  val arrows = allArrows map {key => key -> new ArrowField(key._1, key._2, resources)} toMap

  for((name, arrow) <- arrows) {
    val pos = name._2 match {
      case QRotation.r90 => arrowLeftPosition(name._1)
      case QRotation.r270 => arrowRightPosition(name._1)
    }
    val rot = arrowsRotation(name._1)
    arrow.translate(pos._1, pos._2, 0.0f)
    arrow.rotate(rot)
    addChild(arrow)
  }

  for (quadrant <- Quadrant.values) {
    val centre = quadrantCentre(quadrant)
    quadrants(quadrant).translate(centre._1, centre._2, 0.0f)
    addChild(quadrants(quadrant))
  }

  objectScale = Array(0.25f, 0.25f, 1.0f)

  override def onUpdateResources(): Unit = {
  }

  def quadrantCentre(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-1.5f, -1.5f)
    case Quadrant.second => (1.5f, -1.5f)
    case Quadrant.third => (-1.5f, 1.5f)
    case Quadrant.fourth => (1.5f, 1.5f)
  }

  def arrowRightPosition(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-2.5f, -3.5f)
    case Quadrant.second => (3.5f, -2.5f)
    case Quadrant.third => (-3.5f, 2.5f)
    case Quadrant.fourth => (2.5f, 3.5f)
  }

  def arrowLeftPosition(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => (-3.5f, -2.5f)
    case Quadrant.second => (2.5f, -3.5f)
    case Quadrant.third => (-2.5f, 3.5f)
    case Quadrant.fourth => (3.5f, 2.5f)
  }

  def arrowsRotation(quadrant: Quadrant.Value): Float = quadrant match {
    case Quadrant.first => 0.0f
    case Quadrant.second => 90.0f
    case Quadrant.third => 270.0f
    case Quadrant.fourth => 180.0f
  }

  type Arrow = (Quadrant.Value, QRotation.Value)

  override def onAnimate(dt: Float): Unit = {
    val availableRotations = game.availableRotations
    for((name, arrow) <- arrows) {
      arrow.active = !availableRotations.contains(name._1)
    }
  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
  }

  override def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = {
    try {
      val (near, far) = unProjectMatrices(mvMatrix.get(), pMatrix.get(), clickX, clickY, viewport)
      val I = intersectRayAndXYPlane(near, far)
      return processClick(I(0), I(1))
    } catch {
      case e: UnProjectException =>
        error(e.getMessage)
        return false
      case e: IntersectException =>
        // In current scene configuration this shouldn't happen
        error(e.getMessage)
        return false
    }
  }

  private def matchArrow(x: Float, y: Float): Option[Arrow] = {
    for (quadrant <- Quadrant.values) {
      val al = arrowLeftPosition(quadrant)
      val ar = arrowRightPosition(quadrant)

      //Arrow fields are just squares
      if (x > al._1 - 0.5f && x < al._1 + 0.5f && y > al._2 - 0.5f && y < al._2 + 0.5f) {
        return Some((quadrant, QRotation.r90))
      }
      if (x > ar._1 - 0.5f && x < ar._1 + 0.5f && y > ar._2 - 0.5f && y < ar._2 + 0.5f) {
        return Some((quadrant, QRotation.r270))
      }
    }
    return None
  }

  def processClick(fx: Float, fy: Float): Boolean = {
    val arrow = matchArrow(fx, fy)
    if (arrow.nonEmpty) {
      val a = arrow.get
      try {
        val move = new game.Rotation(a._1, a._2)
        quadrants(a._1).synchronized {
          game.make(move)
          quadrants(a._1).setRotationAnimation(a._2)
        }
        arrows(a).discardIllegal()
      } catch {
        case e: RotationLocked =>
          arrows(a).setIllegal()
      }
      return true
    }
    return false
  }
}
