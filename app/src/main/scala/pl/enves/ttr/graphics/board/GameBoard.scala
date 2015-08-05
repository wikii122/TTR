package pl.enves.ttr.graphics.board

import android.opengl.Matrix
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Algebra

/**
 * Size: Outer: ~2.0x~2.0, inner: ~8.0x~8.0
 * (0.0, 0.0) is in the middle
 */
class GameBoard(game: Game, resources: Resources) extends SceneObject with Logging with Algebra {

  var square: Option[Geometry] = None

  var arrowLeft: Option[Int] = None
  var arrowRight: Option[Int] = None

  var maskShader: Option[MaskShader] = None

  //TODO: Load from settings
  var outerColor1 = Array(179.0f / 255.0f, 179.0f / 255.0f, 179.0f / 255.0f, 1.0f)
  var outerColor2 = Array(255.0f / 255.0f, 255.0f / 255.0f, 255.0f / 255.0f, 1.0f)
  var illegalOuterColor = Array(179.0f / 255.0f, 0.0f / 255.0f, 0.0f / 255.0f, 1.0f)
  var inactiveColor = Array(55.0f / 255.0f, 55.0f / 255.0f, 55.0f / 255.0f, 1.0f)

  val illegalHighlightTime: Long = 2000
  var illegalHighlightTimeSet: Long = 0

  val noColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  val currentPlayerIndicator = new CurrentPlayerIndicator(game, resources)
  currentPlayerIndicator.objectPosition = Array(0.0f, 4.5f, 0.0f)
  currentPlayerIndicator.objectScale = Array(4.0f, 4.0f, 1.0f)
  addChild(currentPlayerIndicator)

  val quadrants = Map(
    (Quadrant.first, new GameQuadrant(game, Quadrant.first, resources)),
    (Quadrant.second, new GameQuadrant(game, Quadrant.second, resources)),
    (Quadrant.third, new GameQuadrant(game, Quadrant.third, resources)),
    (Quadrant.fourth, new GameQuadrant(game, Quadrant.fourth, resources))
  )

  for (quadrant <- Quadrant.values) {
    val centre = quadrantCentre(quadrant)
    quadrants(quadrant).objectPosition = Array(centre._1, centre._2, 0.0f)
    addChild(quadrants(quadrant))
  }

  objectScale = Array(0.25f, 0.25f, 1.0f)

  override def onUpdateResources(): Unit = {
    log("onUpdateResources")

    square = Some(resources.getGeometry(DefaultGeometryId.Square.toString))

    arrowLeft = Some(resources.getTexture(DefaultTextureId.MaskArrowLeft.toString))
    arrowRight = Some(resources.getTexture(DefaultTextureId.MaskArrowRight.toString))

    maskShader = Some(resources.getShader(ShaderId.Mask).asInstanceOf[MaskShader])
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

  def checkIllegal(arrow: Arrow): Boolean = {
    arrow == illegalArrow && System.currentTimeMillis() < illegalHighlightTimeSet + illegalHighlightTime
  }

  def discardIllegal(): Unit = {
    illegalHighlightTimeSet -= illegalHighlightTime
  }

  def setIllegal(arrow: Arrow): Unit = {
    illegalHighlightTimeSet = System.currentTimeMillis()
    illegalArrow = arrow
  }

  var illegalArrow: Arrow = (Quadrant.first, QRotation.r90)

  override def onAnimate(dt: Float): Unit = {

  }

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {
    for (quadrant <- Quadrant.values) {
      drawArrowPair(quadrant, !game.availableRotations.contains(quadrant), mvMatrix, pMatrix)
    }
  }

  def defaultOuterColor(quadrant: Quadrant.Value) = quadrant match {
    case Quadrant.first => outerColor1
    case Quadrant.second => outerColor2
    case Quadrant.third => outerColor2
    case Quadrant.fourth => outerColor1
  }

  def drawArrowPair(quadrant: Quadrant.Value, desaturated: Boolean = false, mvMatrix: MatrixStack, pMatrix: MatrixStack) = {
    val a = arrowLeftPosition(quadrant)
    val b = arrowRightPosition(quadrant)
    val rot = arrowsRotation(quadrant)

    val inner = if (desaturated) {
      inactiveColor
    } else {
      defaultOuterColor(quadrant)
    }

    val outer1 = if (checkIllegal(quadrant, QRotation.r90)) {
      illegalOuterColor
    } else {
      noColor
    }

    val outer2 = if (checkIllegal(quadrant, QRotation.r270)) {
      illegalOuterColor
    } else {
      noColor
    }

    // Arrow Left
    mvMatrix.push()
    Matrix.translateM(mvMatrix.get(), 0, a._1, a._2, 0.0f)
    Matrix.rotateM(mvMatrix.get(), 0, rot, 0.0f, 0.0f, 1.0f)
    maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, inner, outer1, arrowLeft.get))
    mvMatrix.pop()

    // Arrow Right
    mvMatrix.push()
    Matrix.translateM(mvMatrix.get(), 0, b._1, b._2, 0.0f)
    Matrix.rotateM(mvMatrix.get(), 0, rot, 0.0f, 0.0f, 1.0f)
    maskShader.get.draw(mvMatrix, pMatrix, square.get, (noColor, inner, outer2, arrowRight.get))
    mvMatrix.pop()
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

      //Arrows are just squares
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
        game.make(move)
        discardIllegal()
        quadrants(a._1).setRotationAnimation(a._2)
      } catch {
        case e: RotationLocked =>
          setIllegal(a)
      }
      return true
    }
    return false
  }
}
