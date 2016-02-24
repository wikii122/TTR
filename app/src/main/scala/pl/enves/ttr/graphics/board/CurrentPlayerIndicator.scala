package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.animations.InfiniteRotation
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.games.BotGame
import pl.enves.ttr.utils.themes.Theme

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(game: Game)
  extends SceneObject with ColorManip {

  private[this] val player1TurnText = new StaticText(GeometryId.Player1TurnText, TextureId.Font)
  private[this] val player2TurnText = new StaticText(GeometryId.Player2TurnText, TextureId.Font)
  addChild(player1TurnText)
  addChild(player2TurnText)

  private[this] val field = new Field(Quadrant.first)
  addChild(field)

  private[this] var animation: Option[InfiniteRotation] = None

  override protected def onAfterUpdateResources(resources: Resources, screenRatio: Float): Unit = {
    val fieldScale = 0.15f
    val textScale = 0.15f

    val textWidth = Math.max(player1TurnText.getWidth * textScale, player2TurnText.getWidth * textScale)
    val fieldWidth = 1.0f * fieldScale
    val spaceWidth = 0.15f
    val lineWidth = textWidth + spaceWidth + fieldWidth

    player1TurnText.addTranslation(-(lineWidth / 2), 0.0f, 0.0f, true)
    player1TurnText.addScale(textScale, textScale, 1.0f, true)

    player2TurnText.addTranslation(-(lineWidth / 2), 0.0f, 0.0f, true)
    player2TurnText.addScale(textScale, textScale, 1.0f, true)

    field.addTranslation((lineWidth / 2) - (fieldWidth / 2), 0.0f, 0.0f, true)
    field.addScale(fieldScale, fieldScale, 1.0f, true)

    val rotation = field.addRotation(0.0f, 0.0f, 0.0f, 1.0f, false)
    animation = Some(new InfiniteRotation(rotation, 360.0f))
    animation.get.start()
  }

  override protected def onUpdateTheme(theme: Theme): Unit = {
    player1TurnText.setTextColor(theme.color2)
    player2TurnText.setTextColor(theme.color2)
  }

  override protected def onAnimate(dt: Float): Unit = {
    field.setValue(Some(game.player))

    def setTextsStandard(): Unit = {
      field.setVisible(true)
      if (game.player == Player.O) {
        player1TurnText.setVisible(false)
        player2TurnText.setVisible(true)
      } else {
        player2TurnText.setVisible(false)
        player1TurnText.setVisible(true)
      }
    }

    def setTextsBot(showAnything: Boolean): Unit = {
      if (showAnything) {
        field.setVisible(true)
        if (game.locked) {
          player1TurnText.setVisible(false)
          player2TurnText.setVisible(true)
        } else {
          player2TurnText.setVisible(false)
          player1TurnText.setVisible(true)
        }
      } else {
        field.setVisible(false)
        player1TurnText.setVisible(false)
        player2TurnText.setVisible(false)
      }
    }

    def setTextsMulti(): Unit = {
      field.setVisible(true)
      if (game.locked) {
        player1TurnText.setVisible(false)
        player2TurnText.setVisible(true)
      } else {
        player2TurnText.setVisible(false)
        player1TurnText.setVisible(true)
      }
    }

    game.gameType match {
      case Game.STANDARD => setTextsStandard()
      case Game.BOT => setTextsBot(game.asInstanceOf[BotGame].getHuman.isDefined)
      case Game.GPS_MULTIPLAYER => setTextsMulti()
      case Game.REPLAY => setTextsStandard()
    }

    if ((game.locked || game.gameType == Game.REPLAY) && !game.finished) {
      animation.get.pause(false)
    } else {
      animation.get.pause(true)
    }

    animation.get.animate(dt)
  }
}
