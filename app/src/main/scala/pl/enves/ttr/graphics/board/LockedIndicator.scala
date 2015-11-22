package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.Game

/**
 * Display if board is locked in 1x0.25 rectangle
 */
class LockedIndicator(game: Game, resources: Resources) extends SceneObject with ColorManip {

  visible = false

  val lockedText1 = new StaticText(resources, GeometryId.LockedText, TextureId.Font, 1.0f, 0.20f)
  lockedText1.translate(3.5f, 0.0f, 0.0f)
  lockedText1.rotate(-90.0f)
  lockedText1.scale(4.0f, 4.0f, 1.0f)
  addChild(lockedText1)

  val lockedText2 = new StaticText(resources, GeometryId.LockedText, TextureId.Font, 1.0f, 0.20f)
  lockedText2.translate(-3.5f, 0.0f, 0.0f)
  lockedText2.rotate(90.0f)
  lockedText2.scale(4.0f, 4.0f, 1.0f)
  addChild(lockedText2)

  override protected def onUpdateResources(): Unit = {}

  override protected def onUpdateTheme(): Unit = {
    lockedText1.setTextColor(resources.getTheme.outer1)
    lockedText2.setTextColor(resources.getTheme.outer2)
    val noColor: ColorArray = colorTransparent(resources.getTheme.background, 0.0f)
    lockedText1.setTextBackground(noColor)
    lockedText2.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    visible = game.locked
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
