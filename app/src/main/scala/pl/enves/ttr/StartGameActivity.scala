package pl.enves.ttr

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentTransaction}
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.logic.{Game, GameState}
import pl.enves.ttr.utils.start.{BackButtonFragment, MainMenuFragment, OfflineMenuFragment, OnlineMenuFragment}
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.utils.{Code, Configuration, LogoUtils}

class StartGameActivity extends StyledActivity with LogoUtils {
  private[this] val GPS_LAUNCH = 0x14400000

  private[this] lazy val mainMenuFragment = new MainMenuFragment
  private[this] lazy val onlineMenuFragment = new OnlineMenuFragment
  private[this] lazy val offlineMenuFragment = new OfflineMenuFragment

  override def onCreate(savedInstanceState: Bundle) {
    log("Creating")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_game_layout)

    drawUI()
  }

  override def onStart() = {
    log("Starting")
    super.onStart()

    if (Configuration.isFirstRun) {
      Configuration.isFirstRun = false
      launchTutorial()
    } else if (Configuration.isMultiplayerAvailable) {
      PlayServices.connect()
    }
  }

  override def onResume() = {
    super.onResume()
    log("Resuming")

    if (launchedFromGPSNotification) {
      log("Play services intented this activity, jumping to chooser")
      startNetworkGame(Code.INVITATION)
      getIntent.putExtra(Code.LAUNCHED, true)
    }
  }

  override def onActivityResult(request: Int, response: Int, data: Intent): Unit = request match {
    case Code.SIGN_IN => if (response == Activity.RESULT_OK) {
      log(s"Signed in to Google Play Services")
      log(s"Play Services status: ${if (PlayServices.notConnected) "not " else "successfully "}connected")

      if (PlayServices.notConnected) PlayServices.connect()
      else offlineMenuFragment.enableButtons()

      onlineMenuFragment.onConnected()
    } else {
      warn(s"Play Services log in failed with response $response (${Activity.RESULT_OK} is good)")
    }
    case a => error(s"onActivityResult did not match request with id: $a")
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

    val itnt = prepareGameIntent(intent[GameActivity])
    itnt.putExtra(Code.TYPE, Game.STANDARD.toString)
    itnt.start()
  }

  def onConnected() = {
    // disable / enable 'risky' buttons
    onlineMenuFragment.onConnected()

    // if 'online' menu was open and we lost connection
    if (PlayServices.notConnected && onlineMenuFragment.isVisible) {
      getSupportFragmentManager.popBackStack()
    }

    // disable / enable 'online' entry in main menu
    mainMenuFragment.onConnected()
  }

  def startBotGame() = {
    log("Intending to start new BotGame")

    val itnt = prepareGameIntent(intent[GameActivity])
    itnt.putExtra(Code.TYPE, Game.BOT.toString)
    itnt.start()
  }

  def startNetworkGame(code: String) = {
    val itnt = prepareGameIntent(intent[GameActivity])
    itnt.putExtra(Code.TYPE, Game.GPS_MULTIPLAYER.toString)
    itnt.putExtra(Code.DATA, code)
    itnt.start()
  }

  /**
   * Used to continue game in progress
   * Currently, only last game is taken into account.
   */
  def continueGame() = {
    log("Intending to continue previously run game")

    val itnt = intent[GameActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    itnt.putExtra(Code.TYPE, Game.CONTINUE.toString)
    itnt.start()
  }

  /**
   * Used to launch settings activity
   */
  def launchSettings() = {
    log("Intending to continue previously run game")

    val itnt = intent[SettingsActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    itnt.start()
  }

  private[this] def launchedFromGPSNotification =
    (getIntent.getFlags & GPS_LAUNCH) == GPS_LAUNCH &&
      !getIntent.getBooleanExtra(Code.LAUNCHED, false)

  private[this] def launchTutorial() = {
    log("Intending to launch tutorial")

    val itnt = intent[TutorialActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    itnt.putExtra("FIRSTRUN", true)
    itnt.start()
  }

  private[this] def drawUI() = {
    alignLogo()

    val transaction = getSupportFragmentManager.beginTransaction
    transaction.replace(R.id.menuContainer, mainMenuFragment)
    transaction.commit()
  }

  def showOnlineMenu(): Unit = {
    log("Showing online menu")

    val onlineMenuFragment: Fragment = new OnlineMenuFragment
    val backButtonFragment: Fragment = new BackButtonFragment

    val transaction = getSupportFragmentManager.beginTransaction

    transaction.replace(R.id.menuContainer, onlineMenuFragment)
    transaction.replace(R.id.button_back_container, backButtonFragment)
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    transaction.addToBackStack(null)
    transaction.commit()
  }

  def showOfflineMenu(): Unit = {
    log("Showing offline menu")

    val offlineMenuFragment: Fragment = new OfflineMenuFragment
    val backButtonFragment: Fragment = new BackButtonFragment

    val transaction = getSupportFragmentManager.beginTransaction

    transaction.replace(R.id.menuContainer, offlineMenuFragment)
    transaction.replace(R.id.button_back_container, backButtonFragment)
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    transaction.addToBackStack(null)
    transaction.commit()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    setLogoTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    setLogoColorTheme(theme)
  }

  /**
   * Used to check if there is a game in progress.
   */
  private[this] def activeGame: Boolean = GameState.active
}