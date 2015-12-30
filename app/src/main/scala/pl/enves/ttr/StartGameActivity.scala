package pl.enves.ttr

import android.content.{Intent, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.{ImageButton, ViewSwitcher, Button, TextView}
import pl.enves.androidx.color.{ColorUiTweaks, DrawableManip}
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.{Game, GameState, Player}
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.{ThemedOneImageButton, Theme}

class StartGameActivity extends StyledActivity with ColorUiTweaks with DrawableManip {
  private[this] var gameActive = false
  private[this] var viewSwitcher: Option[ViewSwitcher] = None
  private[this] var viewSwitcherSwitched: Boolean = false

  private[this] var newGameButton: Option[(Button, Button)] = None
  private[this] var continueGameButton: Option[(Button, Button)] = None
  private[this] var settingsButton: Option[(Button, Button)] = None

  private[this] var newStandardButton: Option[(Button, Button)] = None
  private[this] var newAIGameButton: Option[(Button, Button)] = None
  private[this] var newNetworkButton: Option[(Button, Button)] = None
  private[this] var backToMainButton: Option[ThemedOneImageButton] = None
  private[this] var gameTypeText: Option[TextView] = None

  private[this] var ticTacText: Option[TextView] = None
  private[this] var turnText: Option[TextView] = None
  private[this] var ttText: Option[TextView] = None
  private[this] var tText: Option[TextView] = None

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    viewSwitcher = Some(find[ViewSwitcher](R.id.menuViewSwitcher))

    newGameButton = Some((find[Button](R.id.button_new), find[Button](R.id.button_new_prompt)))
    continueGameButton = Some((find[Button](R.id.button_continue), find[Button](R.id.button_continue_prompt)))
    settingsButton = Some((find[Button](R.id.button_settings), find[Button](R.id.button_settings_prompt)))

    newStandardButton = Some((find[Button] (R.id.button_create_standard), find[Button](R.id.button_create_standard_prompt)))
    newAIGameButton = Some((find[Button] (R.id.button_create_ai), find[Button](R.id.button_create_ai_prompt)))
    newNetworkButton = Some((find[Button] (R.id.button_create_network), find[Button](R.id.button_create_network_prompt)))
    backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))
    gameTypeText = Some(find[TextView](R.id.text_game_type))

    ticTacText = Some(find[TextView](R.id.text_tic_tac))
    turnText = Some(find[TextView](R.id.text_turn))
    ttText = Some(find[TextView](R.id.text_tt))
    tText = Some(find[TextView](R.id.text_t))

    newGameButton.get onClick flip
    continueGameButton.get onClick continueGame
    settingsButton.get onClick launchSettings

    newStandardButton.get onClick startStandardGame
    newAIGameButton.get onClick startAIGame
    backToMainButton.get onClick unflip

    val inAnimation = AnimationUtils.loadAnimation(this,  android.R.anim.fade_in)
    val outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

    viewSwitcher.get.setInAnimation(inAnimation)
    viewSwitcher.get.setOutAnimation(outAnimation)

    GameState.onDataChanged(enableButtons)
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    enableButtons()

    launchTutorialIfFirstrun()

    if(viewSwitcherSwitched) {
      unflip(viewSwitcher.get)
    }
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
    itnt start()
  }

  private[this] def startAIGame(v: View) = {
    log("Intending to start new AIGame")
    val botSymbol = Player.withName(prefs.get.getString("BOT_SYMBOL", Player.X.toString))
    val humanSymbol = if (botSymbol == Player.X) Player.O else Player.X
    val itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
    itnt putExtra("TYPE", Game.AI.toString)
    itnt putExtra("AI_HUMAN_SYMBOL", humanSymbol.toString)
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

  private[this] def flip(v: View) = {
    log("Showing new game menu")
    viewSwitcher.get.showNext()
    backToMainButton.get.setVisibility(View.VISIBLE)
    viewSwitcherSwitched = true
  }

  private[this] def unflip(v: View) = {
    log("Showing main menu")
    viewSwitcher.get.showPrevious()
    backToMainButton.get.setVisibility(View.GONE)
    viewSwitcherSwitched = false
  }

  private[this] def enableButtons(): Unit = UiThread(() => {
    if (activeGame) {
      continueGameButton.get.enable()
    } else {
      continueGameButton.get.disable()
    }
  })

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    newGameButton.get.setTypeface(typeface)
    continueGameButton.get.setTypeface(typeface)
    settingsButton.get.setTypeface(typeface)

    newStandardButton.get.setTypeface(typeface)
    newAIGameButton.get.setTypeface(typeface)
    newNetworkButton.get.setTypeface(typeface)
    gameTypeText.get.setTypeface(typeface)

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
    continueGameButton.get.setTextColor(colorStateList(theme.color1, 0.25f), colorStateList(theme.color2, 0.25f))
    settingsButton.get.setTextColor(theme.color1, theme.color2)

    newStandardButton.get.setTextColor(theme.color1, theme.color2)
    newAIGameButton.get.setTextColor(theme.color1, theme.color2)
    newNetworkButton.get.setTextColor(theme.color1, theme.color2)
    backToMainButton.get.setColorTheme(theme)
    gameTypeText.get.setTextColor(theme.color2)

    ticTacText.get.setTextColor(theme.color1)
    turnText.get.setTextColor(theme.color2)
    ttText.get.setTextColor(theme.color1)
    tText.get.setTextColor(theme.color2)
  }
}
