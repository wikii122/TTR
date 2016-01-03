package pl.enves.ttr

import android.app.Activity
import android.content.{Intent, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.{ImageButton, ViewSwitcher, Button, TextView}
import com.google.android.gms.games.Games
import pl.enves.androidx.color.{ColorUiTweaks, DrawableManip}
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.logic.{Game, GameState, Player}
import pl.enves.ttr.utils.{dialogs, Configuration}
import pl.enves.ttr.utils.dialogs.{PaidOnlyDialog, Reason}
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.{ThemedOneImageButton, Theme}

class StartGameActivity extends StyledActivity with ColorUiTweaks with DrawableManip {
  private[this] final val SELECT_PLAYERS = 9003
  private[this] var viewSwitcherSwitched = false

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    drawUI()

    GameState.onDataChanged(enableButtons)
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    val viewSwitcher = Some(find[ViewSwitcher](R.id.menuViewSwitcher))

    enableButtons()

    if (viewSwitcherSwitched) {
      unflip(viewSwitcher.get)
    }

    if (Configuration.isFirstRun) {
      Configuration.isFirstRun = false
      launchTutorial()
    } else if (Configuration.isMultiplayerAvailable) {
      PlayServices.connect()
    }
  }

  override def onPause() {
    super.onPause()
  }

  override def onActivityResult(request: Int, response: Int, data: Intent): Unit = request match {
    case SELECT_PLAYERS => if (response == Activity.RESULT_OK) startNetworkGame(data)
      else return
    case PlayServices.SIGN_IN => if (response == Activity.RESULT_OK) {
      log(s"Signed in to Google Play Services")
      log(s"Play Services status: ${if (PlayServices.notConnected) "not " else "successfully "}connected")
      enableButtons()
    } else {
      warn(s"Play Services log in failed with response $response (${Activity.RESULT_OK} is good)")
    }
    case a => error(s"onActivityResult did not match request with id: $a")
  }

  def showDialog(reason: dialogs.Reason) = reason match {
    case dialogs.PaidOnly => val dialog = PaidOnlyDialog.show()
  }

  /**
   * Starts a new, two player game on single device.
   */
  private[this] def startStandardGame(v: View) = {
    log("Intending to start new StandardGame")
    val itnt = prepareGameIntent(intent[GameActivity])
    itnt putExtra("TYPE", Game.STANDARD.toString)
    itnt start ()
  }

  private[this] def startNetworkGame(i: Intent) = {
    log("Intending to start new StandardGame")
    val itnt = prepareGameIntent(intent[GameActivity])
    itnt putExtra ("TYPE", Game.GPS_MULTIPLAYER.toString)
    itnt putExtra ("PLAYERS", i getStringArrayListExtra Games.EXTRA_PLAYER_IDS)
    itnt start ()
  }

  private[this] def startAIGame(v: View) = {
    log("Intending to start new AIGame")
    val botSymbol = Player.withName(prefs.get.getString("BOT_SYMBOL", Player.X.toString))
    val humanSymbol = if (botSymbol == Player.X) Player.O else Player.X
    val itnt = prepareGameIntent(intent[GameActivity])
    itnt putExtra("TYPE", Game.AI.toString)
    itnt putExtra("AI_HUMAN_SYMBOL", humanSymbol.toString)
    itnt start ()
  }

  private[this] def prepareGameIntent(i: Intent): Intent = {
    i addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    i addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP

    return i
  }

  private[this] def startNetworkGame(v: View) = if (Configuration.isMultiplayerAvailable) {
    val intn = PlayServices.getPlayerSelectIntent
    startActivityForResult(intn, SELECT_PLAYERS)
  } else {
    showDialog(dialogs.PaidOnly)
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

  private[this] def launchTutorial() = {
    log("Intending to launch tutorial")
    val itnt = intent[TutorialActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt putExtra("FIRSTRUN", true)
    itnt start()
  }

  private[this] def drawUI() = {
    val viewSwitcher = Some(find[ViewSwitcher](R.id.menuViewSwitcher))

    val newGameButton = Some((find[Button](R.id.button_new), find[Button](R.id.button_new_prompt)))
    val continueGameButton = Some((find[Button](R.id.button_continue), find[Button](R.id.button_continue_prompt)))
    val settingsButton = Some((find[Button](R.id.button_settings), find[Button](R.id.button_settings_prompt)))

    val newStandardButton = Some((find[Button] (R.id.button_create_standard), find[Button](R.id.button_create_standard_prompt)))
    val newAIGameButton = Some((find[Button] (R.id.button_create_ai), find[Button](R.id.button_create_ai_prompt)))
    val newNetworkButton = Some((find[Button] (R.id.button_create_network), find[Button](R.id.button_create_network_prompt)))
    val backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))
    val gameTypeText = Some(find[TextView](R.id.text_game_type))

    val ticTacText = Some(find[TextView](R.id.text_tic_tac))
    val turnText = Some(find[TextView](R.id.text_turn))
    val ttText = Some(find[TextView](R.id.text_tt))
    val tText = Some(find[TextView](R.id.text_t))

    newGameButton.get onClick flip
    continueGameButton.get onClick continueGame
    settingsButton.get onClick launchSettings

    newStandardButton.get onClick startStandardGame
    newAIGameButton.get onClick startAIGame
    newNetworkButton.get onClick startNetworkGame
    backToMainButton.get onClick unflip

    val inAnimation = AnimationUtils.loadAnimation(this,  android.R.anim.fade_in)
    val outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

    viewSwitcher.get.setInAnimation(inAnimation)
    viewSwitcher.get.setOutAnimation(outAnimation)
  }
  private[this] def flip(v: View) = {
    log("Showing new game menu")

    val viewSwitcher = Some(find[ViewSwitcher](R.id.menuViewSwitcher))
    val backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))

    viewSwitcher.get.showNext()
    backToMainButton.get.setVisibility(View.VISIBLE)
    viewSwitcherSwitched = true
  }

  private[this] def unflip(v: View) = {
    log("Showing main menu")

    val viewSwitcher = Some(find[ViewSwitcher](R.id.menuViewSwitcher))
    val backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))

    viewSwitcher.get.showPrevious()
    backToMainButton.get.setVisibility(View.GONE)
    viewSwitcherSwitched = false
  }

  private[this] def enableButtons(): Unit = UiThread {
    val continueGameButton = Some((find[Button](R.id.button_continue), find[Button](R.id.button_continue_prompt)))
    val newNetworkButton = Some((find[Button] (R.id.button_create_network), find[Button](R.id.button_create_network_prompt)))

    if (activeGame) continueGameButton.get.enable()
    else continueGameButton.get.disable()

    if (PlayServices.isConnected) newNetworkButton.get.enable()
    else newNetworkButton.get.disable() // TODO this should be only greyed out
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    val newGameButton = Some((find[Button](R.id.button_new), find[Button](R.id.button_new_prompt)))
    val continueGameButton = Some((find[Button](R.id.button_continue), find[Button](R.id.button_continue_prompt)))
    val settingsButton = Some((find[Button](R.id.button_settings), find[Button](R.id.button_settings_prompt)))

    val newStandardButton = Some((find[Button] (R.id.button_create_standard), find[Button](R.id.button_create_standard_prompt)))
    val newAIGameButton = Some((find[Button] (R.id.button_create_ai), find[Button](R.id.button_create_ai_prompt)))
    val newNetworkButton = Some((find[Button] (R.id.button_create_network), find[Button](R.id.button_create_network_prompt)))
    val backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))
    val gameTypeText = Some(find[TextView](R.id.text_game_type))

    val ticTacText = Some(find[TextView](R.id.text_tic_tac))
    val turnText = Some(find[TextView](R.id.text_turn))
    val ttText = Some(find[TextView](R.id.text_tt))
    val tText = Some(find[TextView](R.id.text_t))

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

    val newGameButton = Some((find[Button](R.id.button_new), find[Button](R.id.button_new_prompt)))
    val continueGameButton = Some((find[Button](R.id.button_continue), find[Button](R.id.button_continue_prompt)))
    val settingsButton = Some((find[Button](R.id.button_settings), find[Button](R.id.button_settings_prompt)))

    val newStandardButton = Some((find[Button] (R.id.button_create_standard), find[Button](R.id.button_create_standard_prompt)))
    val newAIGameButton = Some((find[Button] (R.id.button_create_ai), find[Button](R.id.button_create_ai_prompt)))
    val newNetworkButton = Some((find[Button] (R.id.button_create_network), find[Button](R.id.button_create_network_prompt)))
    val backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))
    val gameTypeText = Some(find[TextView](R.id.text_game_type))

    val ticTacText = Some(find[TextView](R.id.text_tic_tac))
    val turnText = Some(find[TextView](R.id.text_turn))
    val ttText = Some(find[TextView](R.id.text_tt))
    val tText = Some(find[TextView](R.id.text_t))

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
