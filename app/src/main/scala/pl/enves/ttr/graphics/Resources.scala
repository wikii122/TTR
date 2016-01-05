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
import pl.enves.ttr.graphics.geometry.{GeometryId, TextGeometryProvider}
import pl.enves.ttr.graphics.models.Square
import pl.enves.ttr.graphics.shaders._
import pl.enves.ttr.graphics.texture.{CharactersTexture, TextureId}
import pl.enves.ttr.logic.{ReplayGame, Game}
import pl.enves.ttr.utils.themes.Theme

import scala.collection.mutable

class Resources(context: Context, game: Game) extends Logging {

  private val assetManager: AssetManager = context.getResources.getAssets

  private val models: mutable.HashMap[GeometryId.Value, AbstractGeometry] = mutable.HashMap()

  private val textures: mutable.HashMap[TextureId.Value, Int] = mutable.HashMap()

  private var maskShader: Option[MaskShader] = None

  private var _theme: Option[Theme] = None

  private val typeFace = Typeface.createFromAsset(assetManager, "fonts/comfortaa.ttf")

  def createOpenGLResources(): Unit = {
    log("Creating OpenGL Resources")

    addGeometry(GeometryId.Square, Square.getGeometry)

    addTexture(TextureId.MaskCross, new DrawableTexture(context, R.drawable.pat_cross_mod_mask).getTexture)
    addTexture(TextureId.MaskRing, new DrawableTexture(context, R.drawable.pat_ring_mod_mask).getTexture)
    addTexture(TextureId.MaskEmpty, new DrawableTexture(context, R.drawable.pat_empty_mod_mask).getTexture)
    addTexture(TextureId.MaskArrowLeft, new DrawableTexture(context, R.drawable.pat_arrow_left_mod_mask).getTexture)
    addTexture(TextureId.MaskArrowRight, new DrawableTexture(context, R.drawable.pat_arrow_right_mod_mask).getTexture)

    def choosePlayer1String(gameType: Game.Value) = gameType match {
      case Game.STANDARD => context.getString(R.string.board_player1)
      case Game.AI => context.getString(R.string.board_your_turn)
      case Game.GPS_MULTIPLAYER => context.getString(R.string.board_your_turn)
    }

    val player1TurnTextString = if(game.gameType == Game.REPLAY) {
      choosePlayer1String(game.asInstanceOf[ReplayGame].getReplayedGameType)
    } else {
      choosePlayer1String(game.gameType)
    }

    def choosePlayer2String(gameType: Game.Value) = gameType match {
      case Game.STANDARD => context.getString(R.string.board_player2)
      case Game.AI => context.getString(R.string.board_bots_turn)
      case Game.GPS_MULTIPLAYER => context.getString(R.string.board_opponents_turn)
    }

    val player2TurnTextString = if(game.gameType == Game.REPLAY) {
      choosePlayer2String(game.asInstanceOf[ReplayGame].getReplayedGameType)
    } else {
      choosePlayer2String(game.gameType)
    }

    val drawTextString = context.getString(R.string.board_draw)
    val winnerTextString = context.getString(R.string.board_winner)
    val replayTextString = context.getString(R.string.board_replay)

    val words = Array(
      player1TurnTextString,
      player2TurnTextString,
      drawTextString,
      winnerTextString)

    val words2 = Array(
      replayTextString
    )

    val charactersTexture = new CharactersTexture(256, typeFace, allChars(words))
    addTexture(TextureId.Font, charactersTexture.getTexture)

    val charactersTexture2 = new CharactersTexture(512, typeFace, allChars(words2))
    addTexture(TextureId.Font2, charactersTexture2.getTexture)

    addGeometry(GeometryId.Player1TurnText, new TextGeometryProvider(player1TurnTextString, charactersTexture).getGeometry)
    addGeometry(GeometryId.Player2TurnText, new TextGeometryProvider(player2TurnTextString, charactersTexture).getGeometry)
    addGeometry(GeometryId.DrawText, new TextGeometryProvider(drawTextString, charactersTexture).getGeometry)
    addGeometry(GeometryId.WinnerText, new TextGeometryProvider(winnerTextString, charactersTexture).getGeometry)
    addGeometry(GeometryId.ReplayText, new TextGeometryProvider(replayTextString, charactersTexture2).getGeometry)

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

  private def addGeometry(name: GeometryId.Value, geometry: AbstractGeometry): Unit = {
    models.update(name, geometry)
  }

  private def addTexture(name: TextureId.Value, texture: Int): Unit = {
    textures.update(name, texture)
  }

  def getTexture(texture: TextureId.Value): Int = textures(texture)

  def getGeometry(model: GeometryId.Value): AbstractGeometry = models(model)

  def getMaskShader: MaskShader = maskShader.get

  def getTheme: Theme = _theme.get

  def setTheme(theme: Theme): Unit = _theme = Some(theme)

  def getTypeFace: Typeface = typeFace
}
