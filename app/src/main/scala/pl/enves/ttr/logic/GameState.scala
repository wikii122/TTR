package pl.enves.ttr.logic


import android.content.Context
import pl.enves.androidx.Logging
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

import pl.enves.ttr.utils.ExecutorContext._
import scala.concurrent.Future

/**
 * Object used to load last played game
 */
object GameState extends Logging {
  private val field = "gameData"
  private val empty = ""
  private val name = "TTR_GAME_STATE"
  private var dataChanged: () => Unit = ()=>Unit

  def onDataChanged(f: () => Unit) = dataChanged = f

  def store(game: Game)(implicit ctx: Context) = Future {
    sets(ctx, field, game.toJson.compactPrint)
  } andThen { case _ => dataChanged() }

  def load()(implicit ctx: Context): JsValue = gets(ctx, field).parseJson

  def clear()(implicit ctx: Context) = Future {
    sets(ctx, field, empty)
  } andThen { case _ => dataChanged() }

  def active(implicit ctx: Context): Boolean =
    if (gets(ctx, field) == empty) false
    else true

  def nonActive(implicit context: Context) = !active

  // Not setting monitor here, as docs shredpref operations are atomic
  private def gets(ctx: Context, key: String): String =
    ctx.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key, "")

  private def sets(ctx: Context, key: String, value: String) = this.synchronized {
    val editor = ctx.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
    editor.putString(key, value)
    editor.commit()
  }
}
