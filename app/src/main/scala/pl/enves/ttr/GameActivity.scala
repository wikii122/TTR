package pl.enves.ttr

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{View, WindowManager}
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.logic.{GameManager, StandardGame}

/**
 * Core game activity.
 *
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends AppCompatActivity with GameManager {
  private[this] lazy val view: GameView = GameView(this)

  override def onCreate(state: Bundle): Unit = {
    log("Created")

    super.onCreate(state)

    setGui()

    // TODO match game type if more is possible
    game = new StandardGame

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

  def setGui() = {
    getSupportActionBar.hide()

    val window = getWindow
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    getWindow.getDecorView.setSystemUiVisibility(
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )
  }
}
