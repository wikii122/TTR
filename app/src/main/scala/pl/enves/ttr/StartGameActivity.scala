package pl.enves.ttr

import android.content.{Intent, SharedPreferences}
import android.graphics.drawable.BitmapDrawable
import android.graphics.{BitmapFactory, Typeface}
import android.os.Bundle
import android.view.View
import android.widget.{Button, ImageButton, TextView}
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.{ColorUiTweaks, DrawableManip}
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.{Game, GameState, Player}
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme

class StartGameActivity extends StyledActivity with ColorUiTweaks with DrawableManip {
  private[this] var gameActive = false
  private[this] var symbol = Player.X

  private[this] var newGameButton: Option[(Button, Button)] = None
  private[this] var newAIGameButton: Option[(Button, Button)] = None
  private[this] var continueGameButton: Option[(Button, Button)] = None
  private[this] var settingsButton: Option[(Button, Button)] = None
  private[this] var newAIGameSymbol: Option[ImageButton] = None

  private[this] var ticTacText: Option[TextView] = None
  private[this] var turnText: Option[TextView] = None
  private[this] var ttText: Option[TextView] = None
  private[this] var tText: Option[TextView] = None

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    newGameButton = Some((find[Button](R.id.button_create), find[Button](R.id.button_create_prompt)))
    newAIGameButton = Some((find[Button] (R.id.button_create_ai), find[Button](R.id.button_create_ai_prompt)))
    newAIGameSymbol = Some(find[ImageButton](R.id.button_symbol))
    continueGameButton = Some((find[Button](R.id.button_continue), find[Button](R.id.button_continue_prompt)))
    settingsButton = Some((find[Button](R.id.button_settings), find[Button](R.id.button_settings_prompt)))

    ticTacText = Some(find[TextView](R.id.text_tic_tac))
    turnText = Some(find[TextView](R.id.text_turn))
    ttText = Some(find[TextView](R.id.text_tt))
    tText = Some(find[TextView](R.id.text_t))

    newGameButton.get onClick startStandardGame
    newAIGameButton.get onClick startAIGame
    newAIGameSymbol.get onClick changeSymbol
    continueGameButton.get onClick continueGame
    settingsButton.get onClick launchSettings

    GameState.onDataChanged(enableButtons)
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    enableButtons()

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
    if (activeGame) {
      continueGameButton.get.enable()
    } else {
      continueGameButton.get.disable()
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

    val theme = getSavedTheme(getResources, prefs.get)
    maskColors(theme.background, theme.background, theme.color1, drawable)

    newAIGameSymbol.get.setBackground(drawable)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    newGameButton.get.setTypeface(typeface)
    newAIGameButton.get.setTypeface(typeface)
    continueGameButton.get.setTypeface(typeface)
    settingsButton.get.setTypeface(typeface)

    ticTacText.get.setTypeface(typeface)
    turnText.get.setTypeface(typeface)
    ttText.get.setTypeface(typeface)
    tText.get.setTypeface(typeface)

    alignUpsideDownText(turnText.get)
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

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    newGameButton.get.setTextColor(theme.color1, theme.color2)
    newAIGameButton.get.setTextColor(theme.color1, theme.color2)
    continueGameButton.get.setTextColor(colorStateList(theme.color1, 0.25f), colorStateList(theme.color2, 0.25f))
    settingsButton.get.setTextColor(theme.color1, theme.color2)

    ticTacText.get.setTextColor(theme.color1)
    turnText.get.setTextColor(theme.color2)
    ttText.get.setTextColor(theme.color1)
    tText.get.setTextColor(theme.color2)
  }
}
