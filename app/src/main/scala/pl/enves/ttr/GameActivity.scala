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
    view.get.startGame()

    setContentView(view.get)
  }

  override def onPause(): Unit = {
    super.onPause()
    // TODO: closing on lost focus is temporary for activity testing, while it has limited functionalities.
    // It should be removed once restarting game is possible.
    //view.get.onPause()
    this.finish()
  }

  override def onResume(): Unit = {
    super.onResume()
    view.get.onResume()
  }
}