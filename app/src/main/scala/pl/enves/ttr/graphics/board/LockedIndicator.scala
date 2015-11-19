package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.{Game, Quadrant}

/**
 * Display if board is locked in 1x0.25 rectangle
 */
class LockedIndicator(game: Game, resources: Resources) extends SceneObject {

  visible = false

  val lockedText = new StaticText(resources, GeometryId.LockedText, TextureId.Font, 1.0f, 0.20f)
  addChild(lockedText)

  override protected def onUpdateResources(): Unit = {}

  override protected def onUpdateTheme(): Unit = {
    lockedText.setTextColor(resources.getTheme.outer2)
    val noColor: ColorArray = resources.getTheme.background
    noColor(3) = 0.0f //To nicely fade-out on edges
    lockedText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    visible = game.locked
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
