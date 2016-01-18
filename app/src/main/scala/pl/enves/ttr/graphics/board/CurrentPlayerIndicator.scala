package pl.enves.ttr.graphics.board

import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.animations.InfiniteRotation
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.{StaticText, TextAlignment}
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic._

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(game: Game, resources: Resources)
  extends SceneObject with ColorManip {

  private[this] val player1TurnText = new StaticText(resources, GeometryId.Player1TurnText, TextureId.Font, 0.80f, 0.15f, TextAlignment.Left)
  private[this] val player2TurnText = new StaticText(resources, GeometryId.Player2TurnText, TextureId.Font, 0.80f, 0.15f, TextAlignment.Left)
  addChild(player1TurnText)
  addChild(player2TurnText)

  private[this] val field = new Field(game, Quadrant.first, resources)
  addChild(field)

  private[this] var animation: Option[InfiniteRotation] = None

  override protected def onUpdateResources(screenRatio: Float): Unit = {
    player1TurnText.addTranslation(-0.5f, 0.0f, 0.0f, true)
    player2TurnText.addTranslation(-0.5f, 0.0f, 0.0f, true)

    field.addTranslation(0.425f, 0.0f, 0.0f, true)
    field.addScale(0.15f, 0.15f, 1.0f, true)

    val rotation = field.addRotation(0.0f, 0.0f, 0.0f, 1.0f, false)
    animation = Some(new InfiniteRotation(rotation, 360.0f))
    animation.get.start()
  }

  override protected def onUpdateTheme(): Unit = {
    player1TurnText.setTextColor(resources.getTheme.color2)
    player2TurnText.setTextColor(resources.getTheme.color2)

    val noColor: ColorArray = colorTransparent(resources.getTheme.background, 0.0f)
    player1TurnText.setTextBackground(noColor)
    player2TurnText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    field.setValue(Some(game.player), false)

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
      case Game.AI => setTextsBot(game.asInstanceOf[AIGame].getHuman.isDefined)
      case Game.GPS_MULTIPLAYER => setTextsMulti()
      case Game.REPLAY => game.asInstanceOf[ReplayGame].getReplayedGameType match {
        case Game.STANDARD => setTextsStandard()
        case Game.AI => setTextsBot(true)
        case Game.GPS_MULTIPLAYER => setTextsMulti()
      }
    }

    if ((game.locked || game.gameType == Game.REPLAY) && !game.finished) {
      animation.get.pause(false)
    } else {
      animation.get.pause(true)
    }

    animation.get.animate(dt)
  }
}
