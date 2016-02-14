package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.{Game, Quadrant}
import pl.enves.ttr.utils.themes.Theme

/**
 * Display winner in 1x0.25 rectangle
 */
class WinnerIndicator(game: Game) extends SceneObject with ColorManip {

  setVisible(false)

  private[this] val winnerText = new StaticText(GeometryId.WinnerText, TextureId.Font)
  addChild(winnerText)

  private[this] val drawText = new StaticText(GeometryId.DrawText, TextureId.Font)
  addChild(drawText)

  private[this] val field = new Field(Quadrant.second)
  addChild(field)

  override protected def onAfterUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    val fieldScale = 0.15f
    val textScale = 0.15f

    val winnerTextWidth = winnerText.getWidth * textScale
    val fieldWidth = 1.0f * fieldScale
    val spaceWidth = 0.15f
    val lineWidth = winnerTextWidth + spaceWidth + fieldWidth

    winnerText.addTranslation(-(lineWidth / 2), 0.0f, 0.0f, true)
    winnerText.addScale(textScale, textScale, 1.0f, true)

    field.addTranslation((lineWidth / 2) - (fieldWidth / 2), 0.0f, 0.0f, true)
    field.addScale(fieldScale, fieldScale, 1.0f, true)

    val drawTextWidth = drawText.getWidth * textScale
    drawText.addTranslation(-(drawTextWidth / 2), 0.0f, 0.0f, true)
    drawText.addScale(textScale, textScale, 1.0f, true)
  }

  override protected def onUpdateTheme(theme: Theme): Unit = {
    winnerText.setTextColor(theme.color1)
    drawText.setTextColor(theme.color1)
  }

  override protected def onAnimate(dt: Float): Unit = {
    if (game.finished || game.gameType == Game.REPLAY) {
      setVisible(true)
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
      setVisible(false)
    }
  }
}
