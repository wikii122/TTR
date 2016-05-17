package pl.enves.ttr.graphics

/**
 * Objects may utilize different combinations of per-vertex data, textures and shaders
 * which should exist in only one instance - that's what for this class is.
 *
 * TODO: make it better
 */

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import pl.enves.androidx.Logging
import pl.enves.ttr.R
import pl.enves.ttr.graphics.geometry._
import pl.enves.ttr.graphics.shaders._
import pl.enves.ttr.graphics.texture.{AssetTexture, CharactersTexture, TextureId}
import pl.enves.ttr.logic.Game
import pl.enves.ttr.utils.Configuration

import scala.collection.mutable

class Resources(context: Context, game: Game) extends Logging {

  private[this] val assetManager: AssetManager = context.getResources.getAssets

  private[this] val models: mutable.HashMap[GeometryId.Value, Geometry] = mutable.HashMap()

  private[this] val textures: mutable.HashMap[TextureId.Value, Int] = mutable.HashMap()

  private[this] var maskShader: Option[MaskShader] = None

  private[this] val typeFace = Typeface.createFromAsset(assetManager, Configuration.defaultTypefacePath)

  def createOpenGLResources(): Unit = {
    log("Creating OpenGL Resources")

    // clear old objects, just to be sure
    models.clear()
    textures.clear()
    maskShader = None

    addGeometry(GeometryId.Square, Geometry(MeshRectangle2D()))

    addTexture(TextureId.MaskCross, new AssetTexture(context, "images/game/cross_mask.png").getTexture)
    addTexture(TextureId.MaskRing, new AssetTexture(context, "images/game/ring_mask.png").getTexture)
    addTexture(TextureId.MaskEmpty, new AssetTexture(context, "images/game/empty_mask.png").getTexture)
    addTexture(TextureId.MaskArrowLeft, new AssetTexture(context, "images/game/arrow_left_mask.png").getTexture)
    addTexture(TextureId.MaskArrowRight, new AssetTexture(context, "images/game/arrow_right_mask.png").getTexture)

    val player1TurnTextString = game.gameType match {
      case Game.STANDARD => context.getString(R.string.board_player1)
      case Game.BOT => context.getString(R.string.board_your_turn)
      case Game.GPS_MULTIPLAYER => context.getString(R.string.board_your_turn)
      case Game.REPLAY => context.getString(R.string.board_replay)
    }

    val player2TurnTextString = game.gameType match {
      case Game.STANDARD => context.getString(R.string.board_player2)
      case Game.BOT => context.getString(R.string.board_bots_turn)
      case Game.GPS_MULTIPLAYER => context.getString(R.string.board_opponents_turn)
      case Game.REPLAY => context.getString(R.string.board_replay)
    }

    val drawTextString = context.getString(R.string.board_draw)
    val winnerTextString = context.getString(R.string.board_winner)

    val words = Array(
      player1TurnTextString,
      player2TurnTextString,
      drawTextString,
      winnerTextString
    )

    val charactersTexture = new CharactersTexture(256, typeFace, allChars(words))
    addTexture(TextureId.Font, charactersTexture.getTexture)

    addGeometry(GeometryId.Player1TurnText, TextGeometry(player1TurnTextString, charactersTexture))
    addGeometry(GeometryId.Player2TurnText, TextGeometry(player2TurnTextString, charactersTexture))
    addGeometry(GeometryId.DrawText, TextGeometry(drawTextString, charactersTexture))
    addGeometry(GeometryId.WinnerText, TextGeometry(winnerTextString, charactersTexture))

    //create shaders
    maskShader = Some(new MaskShader())
  }

  private def allChars(words: Array[String]): Array[Char] = {
    val set = mutable.Set[Char]()
    for (word <- words) {
      for (char <- word) {
        if (!set.contains(char)) {
          set += char
        }
      }
    }
    return set.toArray
  }

  private def addGeometry(name: GeometryId.Value, geometry: Geometry): Unit = {
    models.update(name, geometry)
  }

  private def addTexture(name: TextureId.Value, texture: Int): Unit = {
    textures.update(name, texture)
  }

  def getTexture(texture: TextureId.Value): Int = textures(texture)

  def getGeometry(model: GeometryId.Value): Geometry = models(model)

  def getMaskShader: MaskShader = maskShader.get

  def getTypeFace: Typeface = typeFace
}
