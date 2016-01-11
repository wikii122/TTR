package pl.enves.ttr.graphics.board

import android.content.Context
import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.{TextAlignment, StaticText}
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.{Game, GameManager, Quadrant}

/**
 * Display winner in 1x0.25 rectangle
 */
class WinnerIndicator(context: Context with GameManager, resources: Resources) extends SceneObject with ColorManip {

  visible = false

  val winnerText = new StaticText(resources, GeometryId.WinnerText, TextureId.Font, 0.80f, 0.15f, TextAlignment.Left)
  addChild(winnerText)

  val drawText = new StaticText(resources, GeometryId.DrawText, TextureId.Font, 1.0f, 0.15f, TextAlignment.Center)
  addChild(drawText)

  val field = new Field(Quadrant.second, resources)
  addChild(field)

  override protected def onUpdateResources(screenRatio: Float): Unit = {
    winnerText.addTranslation(-0.5f, 0.0f, 0.0f, true)

    field.addTranslation(0.425f, 0.0f, 0.0f, true)
    field.addScale(0.15f, 0.15f, 1.0f, true)
  }

  override protected def onUpdateTheme(): Unit = {
    winnerText.setTextColor(resources.getTheme.color1)
    drawText.setTextColor(resources.getTheme.color1)

    val noColor: ColorArray = colorTransparent(resources.getTheme.background, 0.0f)

    winnerText.setTextBackground(noColor)
    drawText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    val game = context.game
    if (game.finished || game.gameType == Game.REPLAY) {
      visible = true
      if (game.winner.isDefined) {
        field.setValue(game.winner)
        field.setVisible(true)
        winnerText.setVisible(true)
        drawText.setVisible(false)
      } else {
        field.setVisible(false)
        winnerText.setVisible(false)
        drawText.setVisible(true)
      }
    } else {
      visible = false
    }
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
