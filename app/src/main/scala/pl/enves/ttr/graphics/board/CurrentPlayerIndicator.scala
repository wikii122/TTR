package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.androidx.color.{ColorImplicits, ColorManip, ColorTypes}
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.{Game, Quadrant}

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(game: Game, resources: Resources) extends SceneObject with ColorManip {

  val playerText = new StaticText(resources, GeometryId.PlayerText, TextureId.Font, 0.75f, 0.20f)
  addChild(playerText)

  val field = new BoardField(Quadrant.first, resources)
  addChild(field)

  override protected def onUpdateResources(): Unit = {
    playerText.translate(-0.125f, 0.0f, 0.0f)

    field.translate(0.375f, 0.0f, 0.0f)
    field.scale(0.2f, 0.2f, 1.0f)
  }

  override protected def onUpdateTheme(): Unit = {
    playerText.setTextColor(resources.getTheme.outer2)
    val noColor: ColorArray = colorTransparent(resources.getTheme.background, 0.0f)
    playerText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    field.value = Some(game.player)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
