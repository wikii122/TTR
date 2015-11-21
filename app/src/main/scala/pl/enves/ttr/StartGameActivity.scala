package pl.enves.ttr

import android.content.{Context, Intent, SharedPreferences}
import android.graphics.drawable.BitmapDrawable
import android.graphics.{BitmapFactory, Typeface}
import android.os.Bundle
import android.view.View
import android.widget.{Button, ImageButton, TextView}
import pl.enves.androidx.ExtendedActivity
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.ColorTypes.ColorAndroid
import pl.enves.androidx.color.{ColorUiTweaks, DrawableManip}
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.{Game, GameState, Player}

class StartGameActivity extends ExtendedActivity with ColorUiTweaks with DrawableManip {
  private[this] var gameActive = false
  private[this] var prefs: Option[SharedPreferences] = None
  private[this] var symbol = Player.X

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    val newGameButton = find[Button](R.id.button_create)
    newGameButton onClick startStandardGame

    val newGamePrompt = find[Button](R.id.button_create_prompt)
    newGamePrompt onClick startStandardGame

    val newAIGameButton = find[Button] (R.id.button_create_ai)
    newAIGameButton onClick startAIGame

    val newAIGamePrompt = find[Button](R.id.button_create_ai_prompt)
    newAIGamePrompt onClick startAIGame

    val newAIGameSymbol = find[ImageButton](R.id.button_symbol)
    newAIGameSymbol onClick changeSymbol

    val continueGameButton = find[Button](R.id.button_continue)
    continueGameButton onClick continueGame

    val continueGamePrompt = find[Button](R.id.button_continue_prompt)
    continueGamePrompt onClick continueGame

    val settingsButton = find[Button](R.id.button_settings)
    settingsButton onClick launchSettings

    val settingsPrompt = find[Button](R.id.button_settings_prompt)
    settingsPrompt onClick launchSettings

    GameState.onDataChanged(enableButtons)

    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    setGui()

    enableButtons()

    applyCustomFont("fonts/comfortaa.ttf")

    val theme = getSavedTheme(prefs.get)
    setColors(theme.background, theme.outer1, theme.outer2)

    symbol = getSavedSymbol
    setSymbolImage(symbol)

    launchTutorialIfFirstrun()
  }

  override def onPause() {
    super.onPause()

    if (symbol != getSavedSymbol) {
      saveSymbol(symbol)
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
    itnt start()
  }

  private[this] def startAIGame(v: View) = {
    log("Intending to start new StandardGame")
    val itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
    itnt putExtra("TYPE", Game.AI.toString)
    itnt putExtra("AI_HUMAN_SYMBOL", symbol.toString)
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

  private[this] def launchTutorialIfFirstrun() = {
    if (prefs.get.getBoolean("FIRSTRUN", true)) {
      val ed: SharedPreferences.Editor = prefs.get.edit()
      ed.putBoolean("FIRSTRUN", false)
      ed.commit()
      log("Intending to launch tutorial")
      val itnt = intent[TutorialActivity]
      itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      itnt putExtra("FIRSTRUN", true)
      itnt start()
    }
  }

  private[this] def enableButtons(): Unit = UiThread(() => {
    //val continueGameButton = find[Button](R.id.button_continue)
    val continueGameButton = find[Button](R.id.button_continue)
    val continueGamePrompt = find[Button](R.id.button_continue_prompt)
    if (activeGame) {
      continueGameButton.enable()
      continueGamePrompt.enable()
    } else {
      continueGameButton.disable()
      continueGamePrompt.disable()
    }
  })

  private[this] def changeSymbol(v: View): Unit = {
    symbol = if (symbol == Player.X) Player.O else Player.X

    setSymbolImage(symbol)
  }

  private[this] def saveSymbol(symbol: Player.Value) = {
    val ed: SharedPreferences.Editor = prefs.get.edit()
    ed.putString("AI_HUMAN_SYMBOL", symbol.toString)
    ed.commit()
  }

  private[this] def getSavedSymbol: Player.Value = {
    return Player.withName(prefs.get.getString("AI_HUMAN_SYMBOL", Player.X.toString))
  }

  private[this] def setSymbolImage(symbol: Player.Value): Unit = {
    val imgRes = if (symbol == Player.X) R.drawable.pat_cross_mod_mask else R.drawable.pat_ring_mod_mask
    val res = getResources
    val drawable = new BitmapDrawable(res, BitmapFactory.decodeResource(res, imgRes))
    drawable.setAntiAlias(true)

    val theme = getSavedTheme(prefs.get)
    maskColors(theme.background, theme.background, theme.outer1, drawable)

    val newAIGameSymbol = find[ImageButton](R.id.button_symbol)
    newAIGameSymbol.setBackground(drawable)
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val newGameButton = find[Button](R.id.button_create)
    newGameButton.setTypeface(typeface)

    val newGamePrompt = find[Button](R.id.button_create_prompt)
    newGamePrompt.setTypeface(typeface)

    val newAIGameButton = find[Button] (R.id.button_create_ai)
    newAIGameButton.setTypeface(typeface)

    val newAIGamePrompt = find[Button](R.id.button_create_ai_prompt)
    newAIGamePrompt.setTypeface(typeface)

    val continueGameButton = find[Button](R.id.button_continue)
    continueGameButton.setTypeface(typeface)

    val continueGamePrompt = find[Button](R.id.button_continue_prompt)
    continueGamePrompt.setTypeface(typeface)

    val settingsButton = find[Button](R.id.button_settings)
    settingsButton.setTypeface(typeface)

    val settingsPrompt = find[Button](R.id.button_settings_prompt)
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

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = GameState.active

  def setColors(background: ColorAndroid, content1: ColorAndroid, content2: ColorAndroid): Unit = {
    val newGameButton = find[Button](R.id.button_create)
    newGameButton.setTextColor(content1)

    val newGamePrompt = find[Button](R.id.button_create_prompt)
    newGamePrompt.setTextColor(content2)

    val newAIGameButton = find[Button] (R.id.button_create_ai)
    newAIGameButton.setTextColor(content1)

    val newAIGamePrompt = find[Button](R.id.button_create_ai_prompt)
    newAIGamePrompt.setTextColor(content2)

    val continueGameButton = find[Button](R.id.button_continue)
    continueGameButton.setTextColor(colorStateList(content1, 0.25f))

    val continueGamePrompt = find[Button](R.id.button_continue_prompt)
    continueGamePrompt.setTextColor(colorStateList(content2, 0.25f))

    val settingsButton = find[Button](R.id.button_settings)
    settingsButton.setTextColor(colorStateList(content1, 0.25f))

    val settingsPrompt = find[Button](R.id.button_settings_prompt)
    settingsPrompt.setTextColor(colorStateList(content2, 0.25f))

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
