package pl.enves.ttr

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentTransaction}
import com.google.android.gms.games.Games
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.logic.{Game, GameState}
import pl.enves.ttr.utils.dialogs.PaidOnlyDialog
import pl.enves.ttr.utils.start.{BackButtonFragment, ChooseGameFragment, MainMenuFragment}
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.utils.{Configuration, LogoUtils, dialogs}

class StartGameActivity extends StyledActivity with LogoUtils {
  private[this] final val SELECT_PLAYERS = 9003

  private[this] lazy val mainMenuFragment: Fragment = new MainMenuFragment

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

    enableButtons()

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
    else return;
    case PlayServices.SIGN_IN => log("Signed in to Google Play Services")
      log(s"Play Services status: ${if (PlayServices.notConnected) "not " else "successfully "}connected")
      drawUI()
    case a => warn(s"onActivityResult did not match request with id: $a")
  }

  def showDialog(reason: dialogs.Reason) = reason match {
    case dialogs.PaidOnly => val dialog = PaidOnlyDialog().show()
  }

  private[this] def startNetworkGame(i: Intent) = {
    log("Intending to start new StandardGame")
    val itnt = prepareGameIntent(intent[GameActivity])
    itnt putExtra("TYPE", Game.GPS_MULTIPLAYER.toString)
    itnt putExtra("PLAYERS", i getStringArrayListExtra Games.EXTRA_PLAYER_IDS)
    itnt start()
  }

  private[this] def prepareGameIntent(i: Intent): Intent = {
    i addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    i addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP

    return i
  }

  /**
   * Starts a new, two player game on single device.
   */
  def startStandardGame() = {
    log("Intending to start new StandardGame")

    unShowNewGameMenu()

    val itnt = prepareGameIntent(intent[GameActivity])
    itnt putExtra("TYPE", Game.STANDARD.toString)
    itnt start()
  }

  def startBotGame() = {
    log("Intending to start new BotGame")

    unShowNewGameMenu()

    val itnt = prepareGameIntent(intent[GameActivity])
    itnt putExtra("TYPE", Game.BOT.toString)
    itnt start()
  }

  def startNetworkGame() = if (Configuration.isMultiplayerAvailable) {

    unShowNewGameMenu()

    val intn = PlayServices.getPlayerSelectIntent
    startActivityForResult(intn, SELECT_PLAYERS)
  } else {
    showDialog(dialogs.PaidOnly)
  }

  /**
   * Used to continue game in progress
   * Currently, only last game is taken into account.
   */
  def continueGame() = {
    log("Intending to continue previously run game")
    val itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt putExtra("TYPE", Game.CONTINUE.toString)
    itnt start()
  }

  /**
   * Used to launch settings activity
   */
  def launchSettings() = {
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
    alignLogo()

    val transaction = getSupportFragmentManager.beginTransaction

    transaction.replace(R.id.menuContainer, mainMenuFragment)
    transaction.commit()
  }

  def showNewGameMenu(): Unit = {
    log("Showing new game menu")

    val chooseGameFragment: Fragment = new ChooseGameFragment
    val backButtonFragment: Fragment = new BackButtonFragment

    val transaction = getSupportFragmentManager.beginTransaction

    transaction.replace(R.id.menuContainer, chooseGameFragment)
    transaction.replace(R.id.button_back_container, backButtonFragment)
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    transaction.addToBackStack(null)
    transaction.commit()
  }

  private[this] def unShowNewGameMenu() = {
    log("Showing main menu")

    getSupportFragmentManager.popBackStack()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    setLogoTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    setLogoColorTheme(theme)
  }

  private[this] def enableButtons(): Unit = UiThread(() => {
    if (mainMenuFragment.isVisible) {
      mainMenuFragment.asInstanceOf[MainMenuFragment].setContinueButtonEnabled(GameState.active)
    }
  })
}
