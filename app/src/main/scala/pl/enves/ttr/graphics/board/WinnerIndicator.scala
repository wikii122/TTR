package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.{Game, Quadrant}

/**
 * Display winner in 1x0.25 rectangle
 */
class WinnerIndicator(game: Game, resources: Resources) extends SceneObject {

  visible = false

  val winnerText = new StaticText(resources, GeometryId.WinnerText, TextureId.Font, 0.75f, 0.25f)
  winnerText.translate(-0.125f, 0.0f, 0.0f)
  addChild(winnerText)

  val field = new BoardField(Quadrant.second, resources)
  field.translate(0.375f, 0.0f, 0.0f)
  field.scale(0.25f, 0.25f, 1.0f)
  addChild(field)

  override protected def onUpdateResources(): Unit = {}

  override protected def onUpdateTheme(): Unit = {
    winnerText.setTextColor(resources.getTheme.outer1)
    val noColor: ColorArray = resources.getTheme.background
    noColor(3) = 0.0f //To nicely fade-out on edges
    winnerText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    if (game.finished) {
      visible = true
      field.value = game.winner
    }
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
