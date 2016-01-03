package pl.enves.ttr.graphics.board

import android.content.Context
import pl.enves.androidx.color.ColorImplicits.AndroidToArray
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.color.ColorTypes.ColorArray
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.geometry.GeometryId
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.graphics.texture.TextureId
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.games.AIGame

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(context: Context with GameManager, resources: Resources) extends SceneObject with ColorManip {
  val omega = 360.0f //degrees per second

  val player1TurnText = new StaticText(resources, GeometryId.Player1TurnText, TextureId.Font, 0.80f, 0.20f)
  val player2TurnText = new StaticText(resources, GeometryId.Player2TurnText, TextureId.Font, 0.80f, 0.20f)
  addChild(player1TurnText)
  addChild(player2TurnText)

  val field = new BoardField(Quadrant.first, resources)
  addChild(field)

  override protected def onUpdateResources(screenRatio: Float): Unit = {
    val s1 = player1TurnText.getWidth / 2
    player1TurnText.translate(-0.5f + player1TurnText.getWidth / 2, 0.0f, 0.0f)
    player2TurnText.translate(-0.5f + player2TurnText.getWidth / 2, 0.0f, 0.0f)

    field.translate(0.4f, 0.0f, 0.0f)
    field.scale(0.2f, 0.2f, 1.0f)
  }

  override protected def onUpdateTheme(): Unit = {
    player1TurnText.setTextColor(resources.getTheme.color2)
    player2TurnText.setTextColor(resources.getTheme.color2)

    val noColor: ColorArray = colorTransparent(resources.getTheme.background, 0.0f)
    player1TurnText.setTextBackground(noColor)
    player2TurnText.setTextBackground(noColor)
  }

  override protected def onAnimate(dt: Float): Unit = {
    val game = context.game
    field.value = Some(game.player)

    if (game.gameType == Game.STANDARD) {
      if (game.player == Player.O) {
        player1TurnText.setVisible(false)
        player2TurnText.setVisible(true)
      } else {
        player2TurnText.setVisible(false)
        player1TurnText.setVisible(true)
      }
    }

    if (game.gameType == Game.AI) {
      val human = game.asInstanceOf[AIGame].getHuman
      if(human.isDefined) {
        field.setVisible(true)
        if (game.player != human.get) {
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

    if (game.locked && (!game.finished || game.isReplaying)) {
      field.rotate(omega * dt)
    } else {
      field.setRotationAngle(0.0f)
    }
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
