package pl.enves.ttr

import android.content.res.ColorStateList
import android.content.{Context, Intent, SharedPreferences}
import android.graphics.{Color, Typeface}
import android.os.Bundle
import android.view.View
import android.widget.TextView
import pl.enves.androidx.ExtendedActivity
import pl.enves.androidx.helpers._
import pl.enves.ttr.graphics.themes.Theme
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

    val newGamePrompt = find[TextView](R.id.button_create_prompt)
    newGamePrompt onClick startStandardGame

    val continueGameButton = find[TextView](R.id.button_continue)
    continueGameButton onClick continueGame

    val continueGamePrompt = find[TextView](R.id.button_continue_prompt)
    continueGamePrompt onClick continueGame

    val settingsButton = find[TextView](R.id.button_settings)
    settingsButton onClick launchSettings

    val settingsPrompt = find[TextView](R.id.button_settings_prompt)
    settingsPrompt onClick launchSettings

    GameState.onDataChanged(enableButtons)
    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))

    applyCustomFont("fonts/comfortaa.ttf")
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    setGui()

    enableButtons()
    setPreviousTheme()
  }

  override def onPause() {
    super.onPause()
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

  /**
   * Used to launch settings activity
   */
  private[this] def launchSettings(v: View) = {
    log("Intending to continue previously run game")
    val itnt = intent[SettingsActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt start()
  }

  private[this] def enableButtons(): Unit = UiThread(() => {
    //val continueGameButton = find[Button](R.id.button_continue)
    val continueGameButton = find[TextView](R.id.button_continue)
    val continueGamePrompt = find[TextView](R.id.button_continue_prompt)
    if (activeGame) {
      continueGameButton.enable()
      continueGamePrompt.enable()
    } else {
      continueGameButton.disable()
      continueGamePrompt.disable()
    }
  })

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val newGameButton = find[TextView](R.id.button_create)
    newGameButton.setTypeface(typeface)

    val newGamePrompt = find[TextView](R.id.button_create_prompt)
    newGamePrompt.setTypeface(typeface)

    val continueGameButton = find[TextView](R.id.button_continue)
    continueGameButton.setTypeface(typeface)

    val continueGamePrompt = find[TextView](R.id.button_continue_prompt)
    continueGamePrompt.setTypeface(typeface)

    val settingsButton = find[TextView](R.id.button_settings)
    settingsButton.setTypeface(typeface)

    val settingsPrompt = find[TextView](R.id.button_settings_prompt)
    settingsPrompt.setTypeface(typeface)

    val ticTacText = find[TextView](R.id.text_tic_tac)
    ticTacText.setTypeface(typeface)

    val turnText = find[TextView](R.id.text_turn)
    turnText.setTypeface(typeface)

    val ttText = find[TextView](R.id.text_tt)
    ttText.setTypeface(typeface)

    val tText = find[TextView](R.id.text_t)
    tText.setTypeface(typeface)

    alignUpsideDownText(turnText)
  }

  private[this] def alignUpsideDownText(text: TextView): Unit = {
    val fm = text.getPaint.getFontMetrics
    val descent = Math.round(fm.descent)
    text.setPadding(0, 0, 0, descent)
  }

  private[this] def getPickedTheme: String = {
    val defaultTheme = Theme(getResources, R.array.theme_five)
    return prefs.get.getString("THEME", defaultTheme.toJsonObject.toString)
  }

  private[this] def setPreviousTheme() = {
    val pickedTheme = Theme(getPickedTheme)
    setColors(pickedTheme.background, pickedTheme.outer1, pickedTheme.outer2)
  }

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = GameState.active

  def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val newGameButton = find[TextView](R.id.button_create)
    newGameButton.setTextColor(content1)

    val newGamePrompt = find[TextView](R.id.button_create_prompt)
    newGamePrompt.setTextColor(content2)

    val continueGameButton = find[TextView](R.id.button_continue)
    continueGameButton.setTextColor(prepareColorStateList(content1))

    val continueGamePrompt = find[TextView](R.id.button_continue_prompt)
    continueGamePrompt.setTextColor(prepareColorStateList(content2))

    val settingsButton = find[TextView](R.id.button_settings)
    settingsButton.setTextColor(prepareColorStateList(content1))

    val settingsPrompt = find[TextView](R.id.button_settings_prompt)
    settingsPrompt.setTextColor(prepareColorStateList(content2))

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

  private[this] def colorTransparent(color: Int, alpha: Float): Int = {
    return Color.argb(Math.round(alpha * 255), Color.red(color), Color.green(color), Color.blue(color))
  }

  private[this] def prepareColorStateList(baseColor: Int): ColorStateList = {
    val states = Array[Array[Int]](
      Array[Int](android.R.attr.state_enabled), // enabled
      Array[Int](-android.R.attr.state_enabled) // disabled
    )

    val colors = Array[Int](
      baseColor,
      colorTransparent(baseColor, 0.25f)
    )

    return new ColorStateList(states, colors)
  }
}
