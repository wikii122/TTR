package pl.enves.ttr

import android.app.Activity
import android.os.Bundle
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.utils.Logging

/**
 * Core game activity.
 *
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends Activity with Logging {
  private[this] var view: Option[GameView] = None

  override def onCreate(state: Bundle): Unit = {
    log("onCreate")
    super.onCreate(state)
    view = Some(GameView(this))
    setContentView(view.get)
  }

  override def onPause(): Unit = {
    super.onPause()
    view.get.onPause()
  }

  override def onResume(): Unit = {
    super.onResume()
    view.get.onResume()
  }
}