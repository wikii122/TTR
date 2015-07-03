package pl.enves.ttr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import pl.enves.ttr.utils.androidx._

class StartGameActivity extends ExtendedActivity {
  private[this] var gameActive = false

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    val newGameButton = find[Button] (R.id.button_create)
    newGameButton onClick startStandardGame

    val continueGameButton = find[Button] (R.id.button_continue)
    continueGameButton onClick continueGame
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    val continueGameButton = find[Button] (R.id.button_continue)
    if (activeGame) continueGameButton.enable()
    else continueGameButton.disable()
  }

  /**
   * Starts a new, two player game on single device.
   */
  private[this] def startStandardGame(v: View) = {
    log("Intending to start new StandardGame")
    val itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
    itnt start()
  }

  /**
   * Used to continue game in progress
   * Currently, only last game is taken into account.
   */
  private[this] def continueGame(v: View) = {
    log("Intending to continue previously run game")
    val itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt start()
  }

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = false
}
