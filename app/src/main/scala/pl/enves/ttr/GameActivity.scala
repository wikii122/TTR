package pl.enves.ttr

import android.app.Activity
import android.os.Bundle
import pl.enves.ttr.graphics.GameView

/**
 * Core game activity.
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends Activity {
  private[this] val view = GameView(this)

  override def onCreate(state: Bundle): Unit = {
    super.onCreate(state)
    setContentView(view)
  }

  override def onPause(): Unit = {
    super.onPause()
    view.onPause()
  }

  override def onResume(): Unit = {
    super.onResume()
    view.onResume()
  }
}