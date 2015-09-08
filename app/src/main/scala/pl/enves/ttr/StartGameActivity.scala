package pl.enves.ttr

import java.util

import android.content.{Context, SharedPreferences, Intent}
import android.os.Bundle
import android.view.View
import android.widget.{ArrayAdapter, Spinner, Button}
import pl.enves.androidx.{ThemePicker, ExtendedActivity}
import pl.enves.androidx.helpers._
import pl.enves.ttr.graphics.themes.ThemeId
import pl.enves.ttr.logic.{Game, GameState}

class StartGameActivity extends ExtendedActivity {
  private[this] var gameActive = false
  private[this] var prefs: Option[SharedPreferences] = None

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    val newGameButton = find[Button] (R.id.button_create)
    newGameButton onClick startStandardGame

    val continueGameButton = find[Button] (R.id.button_continue)
    continueGameButton onClick continueGame

    GameState.onDataChanged(enableButtons)
    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    enableButtons()
    setPreviousTheme()
  }

  override def onPause() {
    super.onPause()

    val themePicker = find[ThemePicker] (R.id.theme_picker)
    if(themePicker.hasChanged) {
      val ed: SharedPreferences.Editor = prefs.get.edit()
      ed.putString("THEME", getTheme())
      ed.commit()
    }
  }


  /**
   * Starts a new, two player game on single device.
   */
  private[this] def startStandardGame(v: View) = {
    log("Intending to start new StandardGame")
    val itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
    itnt putExtra ("TYPE", Game.STANDARD.toString)
    itnt putExtra ("THEME", getTheme())
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
    itnt putExtra ("TYPE", Game.CONTINUE.toString)
    itnt putExtra ("THEME", getTheme())
    itnt start()
  }


  private[this] def enableButtons(): Unit = UiThread ( ()=> {
    val continueGameButton = find[Button](R.id.button_continue)
    if (activeGame) continueGameButton.enable()
    else continueGameButton.disable()
  })

  private[this] def getTheme(): String = {
    val themePicker = find[ThemePicker] (R.id.theme_picker)
    return themePicker.getTheme()
  }

  private[this] def setPreviousTheme() = {
    val themePicker = find[ThemePicker] (R.id.theme_picker)
    themePicker.setTheme(prefs.get.getString("THEME", themePicker.getDefaultTheme))
  }

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = GameState.active
}
