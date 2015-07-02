package pl.enves.ttr

import android.os.Bundle
import android.widget.Button
import pl.enves.ttr.utils.androidx._

class StartGameActivity extends ExtendedActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    val newGameButton = find[Button] (R.id.button_create)
    newGameButton onClick { _ => startStandardGame }

    val continueGameButton = find[Button] (R.id.button_continue)
    continueGameButton onClick { _ => continueGame }
  }

  override def onStart() = {
    super.onStart()

    val continueGameButton = find[Button] (R.id.button_continue)
    if (activeGame) continueGameButton.enable()
    else continueGameButton.disable()
  }

  /**
   * Starts a new, two player game on single device.
   */
  private[this] def startStandardGame = ???

  /**
   * Used to continue game in progress
   * Currently, only last game is taken into account.
   */
  private[this] def continueGame = ???

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = ???
}
