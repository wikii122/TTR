package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.{StaticText, TextAlignment}
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic.Game

/**
 * Display replay text
 */
class ReplayIndicator(game: Game, resources: Resources) extends SceneObject with ColorManip {

  setVisible(false)

  private[this] val replayText = new StaticText(resources, GeometryId.ReplayText, TextureId.Font2, 1.0f, 0.25f, TextAlignment.Center)
  addChild(replayText)

  override protected def onUpdateTheme(): Unit = {
    replayText.setTextColor(colorTransparent(resources.getTheme.winner, 0.5f))
    val noColor = colorTransparent(resources.getTheme.winner, 0.0f)
    replayText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    if (game.gameType == Game.REPLAY) {
      setVisible(true)
    } else {
      setVisible(false)
    }
  }
}
