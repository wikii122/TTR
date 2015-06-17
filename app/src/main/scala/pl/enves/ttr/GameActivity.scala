package pl.enves.ttr

import android.app.Activity
import android.os.Bundle
import android.view.View
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.utils.Logging

/**
 * Core game activity.
 *
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends Activity with Logging {
  private[this] lazy val view: GameView = GameView(this)

  override def onCreate(state: Bundle): Unit = {
    log("Created")

    super.onCreate(state)

    getWindow.getDecorView.setSystemUiVisibility(
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    )

    view.startGame()
    setContentView(view)
  }

  override def onPause(): Unit = {
    log("Paused")

    super.onPause()
    view.onPause()
  }

  override def onResume(): Unit = {
    log("Resumed")

    super.onResume()
    view.onResume()
  }

  override def onStop() = {
    log("Stopped")
    
    super.onStop()
    // TODO: Remove in production
    this.finish()
  }
}