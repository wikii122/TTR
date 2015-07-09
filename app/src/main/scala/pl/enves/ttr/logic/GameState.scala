package pl.enves.ttr.logic

import android.content.Context
import pl.enves.androidx.Logging
import spray.json._
import pl.enves.ttr.utils.JsonProtocol._
/**
 * Object used to load last played game
 */
object GameState extends Logging {
  private val field = "gameData"
  private val empty = ""
  private val name = "TTR_GAME_STATE"

  def store(game: Game)(implicit context: Context) = {
    log(game.toJson.compactPrint)
    sets(context, field, game.toJson.compactPrint)
  }

  def load()(implicit context: Context): JsValue = gets(context, field).parseJson

  def clear()(implicit context: Context) = sets(context, field, empty)

  def active(implicit context: Context): Boolean =
    if (gets(context, field) == empty) false
    else true
  def nonActive(implicit context: Context) = !active

  private def gets(context: Context, key: String): String =
    context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key, "")

  private def sets(context: Context, key: String, value: String) = {
    val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
    editor.putString(key, value)
    editor.commit()
  }
}
