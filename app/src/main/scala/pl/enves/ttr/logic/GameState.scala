package pl.enves.ttr.logic


import android.content.Context
import pl.enves.androidx.Logging
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

import pl.enves.ttr.utils.ExecutorContext._
import scala.concurrent.Future
import pl.enves.androidx.context.ContextRegistry
/**
 * Object used to load last played game
 */
object GameState extends Logging {
  private val field = "gameData"
  private val empty = ""
  private val name = "TTR_GAME_STATE"
  private var dataChanged: () => Unit = ()=>Unit

  def onDataChanged(f: () => Unit) = dataChanged = f

  def store(game: Game) = Future {
    sets(field, game.toJson.compactPrint)
  } andThen { case _ => dataChanged() }

  def load(): JsValue = gets(field).parseJson

  def clear() = Future {
    sets(field, empty)
  } andThen { case _ => dataChanged() }

  def active: Boolean =
    if (gets(field) == empty) false
    else true

  def nonActive = !active

  // Not setting monitor here, as docs shredpref operations are atomic
  private def gets(key: String): String =
    ContextRegistry.context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key, "")

  private def sets(key: String, value: String) = this.synchronized {
    val editor = ContextRegistry.context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
    editor.putString(key, value)
    editor.commit()
  }
}
