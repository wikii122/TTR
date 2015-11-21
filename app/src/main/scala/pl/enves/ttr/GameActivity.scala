package pl.enves.ttr

import android.os.Bundle
import android.view.{View, WindowManager}
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.utils.themes.Theme
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

    val b:Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())
    Game withName (b getString "TYPE") match {
        case Game.STANDARD =>
          game = Game.create(Game.STANDARD)
          view.startGame()
        case Game.AI =>
          game = Game.create(Game.AI)
          view.startGame()
        case Game.CONTINUE =>
          game = Game.load(GameState.load())
        case s =>
          throw new IllegalArgumentException(s"Invalid game type: $s")
    }
    view.setTheme(Theme(b.getString("THEME")))
    setContentView(view)
  }

  override def onPause(): Unit = {
    log("Pausing")

    super.onPause()
    view.onPause()

    if (game.nonFinished) GameState store game
    else GameState clear()
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
}
