package pl.enves.ttr.graphics.board

import android.graphics.Color
import android.opengl.Matrix
import pl.enves.ttr.graphics.models.DefaultGeometryId
import pl.enves.ttr.graphics.shaders.TextureShader
import pl.enves.ttr.graphics._
import pl.enves.ttr.graphics.text.StaticText
import pl.enves.ttr.logic.Player
import pl.enves.ttr.logic.Game

/**
 * Display current player in 1x0.25 rectangle
 */
class CurrentPlayerIndicator(game: Game, resources: Resources) extends SceneObject with Coordinates {

  val playerText = new StaticText("Player:", resources, 0.75f, 0.25f, Color.CYAN)
  playerText.objectPosition = Array(-0.5f, 0.0f, 0.0f)
  addChild(playerText)

  var ring: Option[Int] = None
  var cross: Option[Int] = None

  var textureShader: Option[TextureShader] = None

  var rectangle: Option[Geometry] = None

  override protected def onUpdateResources(): Unit = {
    rectangle = Some(resources.getGeometry(DefaultGeometryId.Square.toString))

    ring = Some(resources.getTexture(DefaultTextureId.Ring.toString))
    cross = Some(resources.getTexture(DefaultTextureId.Cross.toString))

    textureShader = Some(resources.getShader(ShaderId.Texture).asInstanceOf[TextureShader])
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
      case Player.O => textureShader.get.draw(rectangle.get, ring.get)
      case Player.X => textureShader.get.draw(rectangle.get, cross.get)
    }
    MVMatrix.pop()
  }
}
