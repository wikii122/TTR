package pl.enves.ttr.graphics.board

import android.graphics.Color
import android.opengl.Matrix
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.logic.{Quadrant, Game, Player}

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(game: Game, resources: Resources) extends SceneObject {

  //TODO: From settings
  val textColor = Color.rgb(179, 179, 179)
  val playerText = new StaticText("Player:", resources, 0.75f, 0.25f, textColor)
  playerText.translate(-0.125f, 0.0f, 0.0f)
  addChild(playerText)

  val field = new BoardField(Quadrant.first, resources)
  field.translate(0.375f, 0.0f, 0.0f)
  field.scale(0.25f, 0.25f, 1.0f)
  addChild(field)

  override protected def onUpdateResources(): Unit = {}

  override protected def onAnimate(dt: Float): Unit = {
    field.value = Some(game.player)
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int], mvMatrix: MatrixStack, pMatrix: MatrixStack): Boolean = false

  override protected def onDraw(mvMatrix: MatrixStack, pMatrix: MatrixStack): Unit = {}
}
