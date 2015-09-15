package pl.enves.ttr

import android.content.{Context, Intent, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import pl.enves.androidx.helpers._
import pl.enves.androidx.{ExtendedActivity, ThemePicker}
import pl.enves.ttr.logic.{Game, GameState}

class StartGameActivity extends ExtendedActivity {
  private[this] var gameActive = false
  private[this] var prefs: Option[SharedPreferences] = None

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    val newGameButton = find[TextView](R.id.button_create)
    newGameButton onClick startStandardGame

    val continueGameButton = find[TextView](R.id.button_continue)
    continueGameButton onClick continueGame

    GameState.onDataChanged(enableButtons)
    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))

    applyCustomFont("fonts/comfortaa.ttf")
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    enableButtons()
    setPreviousTheme()
  }

  override def onPause() {
    super.onPause()

    val themePicker = find[ThemePicker](R.id.theme_picker)
    if (themePicker.hasChanged) {
      val ed: SharedPreferences.Editor = prefs.get.edit()
      ed.putString("THEME", getPickedTheme)
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
    itnt putExtra("TYPE", Game.STANDARD.toString)
    itnt putExtra("THEME", getPickedTheme)
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
    itnt putExtra("TYPE", Game.CONTINUE.toString)
    itnt putExtra("THEME", getPickedTheme)
    itnt start()
  }


  private[this] def enableButtons(): Unit = UiThread(() => {
    //val continueGameButton = find[Button](R.id.button_continue)
    val continueGameButton = find[TextView](R.id.button_continue)
    if (activeGame) continueGameButton.enable()
    else continueGameButton.disable()
  })

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val newGameButton = find[TextView](R.id.button_create)
    newGameButton.setTypeface(typeface)

    val continueGameButton = find[TextView](R.id.button_continue)
    continueGameButton.setTypeface(typeface)

    val pickThemeText = find[TextView](R.id.text_pick_theme)
    pickThemeText.setTypeface(typeface)

    val ticTacText = find[TextView](R.id.text_tic_tac)
    ticTacText.setTypeface(typeface)

    val turnText = find[TextView](R.id.text_turn)
    turnText.setTypeface(typeface)

    val ttText = find[TextView](R.id.text_tt)
    ttText.setTypeface(typeface)

    val tText = find[TextView](R.id.text_t)
    tText.setTypeface(typeface)
  }

  private[this] def getPickedTheme: String = {
    val themePicker = find[ThemePicker](R.id.theme_picker)
    return themePicker.getCurrentJSON
  }

  private[this] def setPreviousTheme() = {
    val themePicker = find[ThemePicker](R.id.theme_picker)
    themePicker.setColorChanger(setColors)
    themePicker.setCurrentFromJSON(prefs.get.getString("THEME", themePicker.getDefaultJSON))
  }

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = GameState.active

  def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val newGameButton = find[TextView](R.id.button_create)
    newGameButton.setTextColor(content1)

    val continueGameButton = find[TextView](R.id.button_continue)
    continueGameButton.setTextColor(content1)

    val pickThemeText = find[TextView](R.id.text_pick_theme)
    pickThemeText.setTextColor(content1)

    val ticTacText = find[TextView](R.id.text_tic_tac)
    ticTacText.setTextColor(content1)

    val turnText = find[TextView](R.id.text_turn)
    turnText.setTextColor(content2)

    val ttText = find[TextView](R.id.text_tt)
    ttText.setTextColor(content1)

    val tText = find[TextView](R.id.text_t)
    tText.setTextColor(content2)

    newGameButton.getRootView.setBackgroundColor(background)
  }
}
