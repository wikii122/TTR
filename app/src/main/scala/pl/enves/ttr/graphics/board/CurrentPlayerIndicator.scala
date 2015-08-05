package pl.enves.ttr.graphics.board

import android.graphics.Color
import android.opengl.Matrix
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders.MaskShader
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.logic.{Game, Player}

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(game: Game, resources: Resources) extends SceneObject {

  //TODO: From settings
  val textColor = Color.rgb(179, 179, 179)
  val playerText = new StaticText("Player:", resources, 0.75f, 0.25f, textColor)
  playerText.objectPosition = Array(-0.5f, 0.0f, 0.0f)
  addChild(playerText)

  var ring: Option[Int] = None
  var cross: Option[Int] = None

  var maskShader: Option[MaskShader] = None

  var square: Option[Geometry] = None

  //TODO: Load from settings
  var crossColor = Array(27.0f / 255.0f, 20.0f / 255.0f, 100.0f / 255.0f, 1.0f)
  var ringColor = Array(27.0f / 255.0f, 20.0f / 255.0f, 100.0f / 255.0f, 1.0f)
  var outerColor = Array(179.0f / 255.0f, 179.0f / 255.0f, 179.0f / 255.0f, 1.0f)
  val noColor = Array(0.0f, 0.0f, 0.0f, 0.0f)

  override protected def onUpdateResources(): Unit = {
    square = Some(resources.getGeometry(DefaultGeometryId.Square.toString))

    ring = Some(resources.getTexture(DefaultTextureId.MaskRing.toString))
    cross = Some(resources.getTexture(DefaultTextureId.MaskCross.toString))

    maskShader = Some(resources.getShader(ShaderId.Mask).asInstanceOf[MaskShader])
  }

  override protected def onAnimate(dt: Float): Unit = {
    //objectScale(1)=objectScale(1)*1.001f;
    //objectRotationAngle += 1;
  }

  override protected def onClick(clickX: Float, clickY: Float, viewport: Array[Int]): Boolean = false

  override protected def onDraw(): Unit = {
    Matrix.translateM(MVMatrix(), 0, 0.375f, 0.0f, 0.0f)
    MVMatrix.push()
    Matrix.scaleM(MVMatrix(), 0, 0.25f, 0.25f, 0.25f)
    game.player match {
      case Player.O => maskShader.get.draw(square.get, (noColor, ringColor, outerColor, ring.get))
      case Player.X => maskShader.get.draw(square.get, (noColor, crossColor, outerColor, cross.get))
    }
    MVMatrix.pop()
  }
}
