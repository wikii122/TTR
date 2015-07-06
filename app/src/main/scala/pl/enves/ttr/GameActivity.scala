package pl.enves.ttr

import android.os.Bundle
import android.view.{View, WindowManager}
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.logic.{Game, GameState, GameManager, StandardGame}
import pl.enves.androidx.ExtendedActivity

import scala.concurrent.Future

/**
 * Core game activity.
 *
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends ExtendedActivity with GameManager {
  private[this] lazy val view: GameView = GameView(this)

  override def onCreate(state: Bundle): Unit = {
    log("Creating")

    super.onCreate(state)

    setGui()

    Option(getIntent.getExtras) getOrElse (throw new UninitializedError()) getString "TYPE" match {
        case Game.STANDARD => game = new StandardGame
        case Game.CONTINUE => game = GameState.load()
        case s => throw new IllegalArgumentException(s"Invalid game type: $s")
    }

    view.startGame()
    setContentView(view)
  }

  override def onPause(): Unit = {
    log("Pausing")

    super.onPause()
    view.onPause()

    GameState store game
  }

  override def onResume(): Unit = {
    log("Resuming")
    super.onResume()
    view.onResume()
  }

  override def onStop() = {
    log("Stopping")
    super.onStop()

    // There is no point to keep finished game in memory.
    if (game.finished) {
      GameState.clear()
      this.finish()
    }
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
