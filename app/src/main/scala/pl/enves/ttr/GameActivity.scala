package pl.enves.ttr

import android.os.Bundle
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme

/**
 * Core game activity.
 *
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends StyledActivity with GameManager {
  private[this] lazy val view: GameView = GameView(this)

  override def onCreate(state: Bundle): Unit = {
    log("Creating")

    super.onCreate(state)

    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())
    Game withName (b getString "TYPE") match {
      case Game.STANDARD =>
        game = Game.create(Game.STANDARD)
        view.startGame()
      case Game.AI =>
        game = Game.createAI(Player.withName(b.getString("AI_HUMAN_SYMBOL")))
        view.startGame()
      case Game.CONTINUE =>
        game = Game.load(GameState.load())
      case s =>
        throw new IllegalArgumentException(s"Invalid game type: $s")
    }
    setContentView(view)
  }

  override def setColorTheme(theme: Theme): Unit = {
    view.setTheme(theme)
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
