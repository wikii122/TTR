package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.{StaticText, TextAlignment}
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.{Game, Quadrant}

/**
 * Display winner in 1x0.25 rectangle
 */
class WinnerIndicator(game: Game, resources: Resources) extends SceneObject with ColorManip {

  setVisible(false)

  private[this] val winnerText = new StaticText(resources, GeometryId.WinnerText, TextureId.Font, 0.80f, 0.15f, TextAlignment.Left)
  addChild(winnerText)

  private[this] val drawText = new StaticText(resources, GeometryId.DrawText, TextureId.Font, 1.0f, 0.15f, TextAlignment.Center)
  addChild(drawText)

  private[this] val field = new Field(game, Quadrant.second, resources)
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
    if (game.finished || game.gameType == Game.REPLAY) {
      setVisible(true)
      if (game.winner.isDefined) {
        field.setValue(game.winner, false)
        field.setVisible(true)
        winnerText.setVisible(true)
        drawText.setVisible(false)
      } else {
        field.setVisible(false)
        winnerText.setVisible(false)
        drawText.setVisible(true)
      }
    } else {
      setVisible(false)
    }
  }
}
