package pl.enves.ttr

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view._
import android.widget._
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.helpers._
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.logic._
import pl.enves.ttr.logic.ai.{BestMoveHeuristics, PositionsChoosing}
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.{ThemedOneImageButton, Theme}

/**
 * Core game activity.
 *
 * Basically it wraps and configures all things that can display other things.
 */
class GameActivity extends StyledActivity with GameManager with ColorManip {
  private[this] lazy val view: GameView = GameView(this, showMenu)
  private[this] var afterGameMenuLayer: Option[View] = None

  private[this] var playAgainButton: Option[(Button, Button)] = None
  private[this] var gameCourseButton: Option[(Button, Button)] = None
  private[this] var contemplateButton: Option[(Button, Button)] = None
  private[this] var backToMainButton: Option[ThemedOneImageButton] = None

  private[this] var chooseSymbolLayer: Option[View] = None
  private[this] var chooseSymbolText: Option[TextView] = None
  private[this] var chooseXButton: Option[ThemedOneImageButton] = None
  private[this] var chooseOButton: Option[ThemedOneImageButton] = None

  //TODO: Remove in production
  private[this] var maxTimeSpinner: Option[Spinner] = None
  private[this] var maxTimeText: Option[TextView] = None
  private[this] var maxDepthSpinner: Option[Spinner] = None
  private[this] var maxDepthText: Option[TextView] = None
  private[this] var adaptiveDepthSwitch: Option[Switch] = None
  private[this] var positionsChoosingSpinner: Option[Spinner] = None
  private[this] var positionsChoosingText: Option[TextView] = None
  private[this] var bestMoveHeuristicsSpinner: Option[Spinner] = None
  private[this] var bestMoveHeuristicsText: Option[TextView] = None
  private[this] var randomizeDecisionsSwitch: Option[Switch] = None

  private[this] var botTestingLayer: Option[View] = None
  private[this] var botEnableSwitch: Option[Switch] = None
  private[this] var botStatusText: Option[TextView] = None

  override def onCreate(state: Bundle): Unit = {
    log("Creating")

    super.onCreate(state)

    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())
    Game withName (b getString "TYPE") match {
      case Game.STANDARD =>
        game = Game.create(Game.STANDARD)
        view.startGame()
      case Game.AI =>
        game = Game.create(Game.AI)
      case Game.CONTINUE =>
        game = Game.load(GameState.load())
      case s =>
        throw new IllegalArgumentException(s"Invalid game type: $s")
    }

    if (game.isReplaying) {
      view.startReplaying()
    }

    val frameLayout = new FrameLayout(this)
    val gameLayoutParams = new ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT)
    frameLayout.addView(view, gameLayoutParams)

    val inflater = getLayoutInflater
    afterGameMenuLayer = Some(inflater.inflate(R.layout.after_game_menu_layout, null))
    afterGameMenuLayer.get.setVisibility(View.GONE)

    val afterGameMenuLayoutParams = new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
      Gravity.CENTER)
    frameLayout.addView(afterGameMenuLayer.get, afterGameMenuLayoutParams)

    chooseSymbolLayer = Some(inflater.inflate(R.layout.choose_symbol_layout, null))
    chooseSymbolLayer.get.setVisibility(View.GONE)

    val chooseSymbolLayoutParams = new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
      Gravity.TOP)
    frameLayout.addView(chooseSymbolLayer.get, chooseSymbolLayoutParams)

    botTestingLayer = Some(inflater.inflate(R.layout.bot_testing_layout, null))
    botTestingLayer.get.setVisibility(View.GONE)

    val botTestingLayoutParams = new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT,
      Gravity.BOTTOM)

    frameLayout.addView(botTestingLayer.get, botTestingLayoutParams)

    setContentView(frameLayout)

    playAgainButton = Some((find[Button](R.id.button_play_again), find[Button](R.id.button_play_again_prompt)))
    gameCourseButton = Some((find[Button](R.id.button_game_course), find[Button](R.id.button_game_course_prompt)))
    contemplateButton = Some((find[Button](R.id.button_contemplate), find[Button](R.id.button_contemplate_prompt)))
    backToMainButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_back_to_main), R.drawable.ic_action_back_mask))

    playAgainButton.get onClick onPlayAgain
    gameCourseButton.get onClick onReplay
    contemplateButton.get onClick onCloseMenu
    backToMainButton.get onClick onBackToMainMenu

    chooseSymbolText = Some(find[TextView](R.id.text_choose_symbol))
    chooseXButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_symbol_X), R.drawable.pat_cross_mod_mask))
    chooseOButton = Some(new ThemedOneImageButton(this, find[ImageButton](R.id.button_symbol_O), R.drawable.pat_ring_mod_mask))

    chooseXButton.get onClick onPlayWithBotAsX
    chooseOButton.get onClick onPlayWithBotAsO

    maxTimeSpinner = Some(find[Spinner](R.id.spinner_max_time))
    maxTimeText = Some(find[TextView](R.id.text_max_time))

    maxDepthSpinner = Some(find[Spinner](R.id.spinner_max_depth))
    maxDepthText = Some(find[TextView](R.id.text_max_depth))

    adaptiveDepthSwitch = Some(find[Switch](R.id.adaptive_depth_switch))

    positionsChoosingSpinner = Some(find[Spinner](R.id.spinner_positions_choosing))
    positionsChoosingText = Some(find[TextView](R.id.text_positions_choosing))

    bestMoveHeuristicsSpinner = Some(find[Spinner](R.id.spinner_best_move_heuristics))
    bestMoveHeuristicsText = Some(find[TextView](R.id.text_best_move_heuristics))

    randomizeDecisionsSwitch = Some(find[Switch](R.id.randomize_decisions_switch))

    maxTimeSpinner.get.setSelection(5)
    maxDepthSpinner.get.setSelection(3)
    positionsChoosingSpinner.get.setAdapter(PositionsChoosing.getAdapter(this))
    positionsChoosingSpinner.get.setSelection(PositionsChoosing.Reasonable.id)
    bestMoveHeuristicsSpinner.get.setAdapter(BestMoveHeuristics.getAdapter(this))
    bestMoveHeuristicsSpinner.get.setSelection(BestMoveHeuristics.PreviousIteration.id)

    botEnableSwitch = Some(find[Switch](R.id.bot_enabled_switch))
    botEnableSwitch.get onCheck onBotEnabledCheck
    botStatusText = Some(find[TextView](R.id.bot_status_text))

    if(game.gameType == Game.AI) {
      if(game.asInstanceOf[AIGame].getHuman.isEmpty) {
        showChooser()
      }
      if(!game.isReplaying) {
        showBotTesting()
      }
    }
  }

  override def onTouchEvent(e: MotionEvent): Boolean = {
    log("touched")
    return super.onTouchEvent(e)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    playAgainButton.get.setTypeface(typeface)
    gameCourseButton.get.setTypeface(typeface)
    contemplateButton.get.setTypeface(typeface)

    chooseSymbolText.get.setTypeface(typeface)

    maxDepthText.get.setTypeface(typeface)
    adaptiveDepthSwitch.get.setTypeface(typeface)
    maxTimeText.get.setTypeface(typeface)
    positionsChoosingText.get.setTypeface(typeface)
    bestMoveHeuristicsText.get.setTypeface(typeface)
    randomizeDecisionsSwitch.get.setTypeface(typeface)

    botEnableSwitch.get.setTypeface(typeface)
    botStatusText.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    view.setTheme(theme)

    afterGameMenuLayer.get.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    playAgainButton.get.setTextColor(theme.color1, theme.color2)
    gameCourseButton.get.setTextColor(theme.color1, theme.color2)
    contemplateButton.get.setTextColor(theme.color1, theme.color2)
    backToMainButton.get.setColorTheme(theme)

    chooseSymbolLayer.get.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    chooseSymbolText.get.setTextColor(theme.color2)
    chooseXButton.get.setColorTheme(theme)
    chooseOButton.get.setColorTheme(theme)

    maxTimeText.get.setTextColor(theme.color2)
    maxDepthText.get.setTextColor(theme.color2)
    adaptiveDepthSwitch.get.setTextColor(theme.color2)
    positionsChoosingText.get.setTextColor(theme.color2)
    bestMoveHeuristicsText.get.setTextColor(theme.color2)
    randomizeDecisionsSwitch.get.setTextColor(theme.color2)

    botEnableSwitch.get.setTextColor(theme.color2)
    botStatusText.get.setTextColor(theme.color2)
  }

  override def onPause(): Unit = {
    log("Pausing")

    super.onPause()
    view.onPause()

    if (game.canBeSaved) GameState store game
    else GameState clear()
  }

  override def onResume(): Unit = {
    log("Resuming")
    super.onResume()
    view.onResume()
  }

  override def onStop() = {
    log("Stopping")
    super.onStop()

    // There is no point to keep game that cannot be saved.
    if (!game.canBeSaved) {
      GameState.clear()
      this.finish()
    }
  }

  def showMenu(winner: Option[Player.Value]): Unit = {
    log("showing game menu")
    runOnUiThread(new Runnable() {
      override def run(): Unit = {
        afterGameMenuLayer.get.setVisibility(View.VISIBLE)

        //make sure that all buttons are visible, android gets crazy sometimes
        playAgainButton.get.setVisibility(View.VISIBLE)
        gameCourseButton.get.setVisibility(View.VISIBLE)
        contemplateButton.get.setVisibility(View.VISIBLE)
        backToMainButton.get.setVisibility(View.VISIBLE)
      }
    })
  }

  private[this] def onCloseMenu(v: View): Unit = {
    afterGameMenuLayer.get.setVisibility(View.GONE)

    //make sure that all buttons are gone, android gets crazy sometimes
    playAgainButton.get.setVisibility(View.GONE)
    gameCourseButton.get.setVisibility(View.GONE)
    contemplateButton.get.setVisibility(View.GONE)
    backToMainButton.get.setVisibility(View.GONE)
  }

  def showChooser(): Unit = {
    log("showing chooser")
    chooseSymbolLayer.get.setVisibility(View.VISIBLE)

    //make sure that all buttons are visible, android gets crazy sometimes
    chooseSymbolText.get.setVisibility(View.VISIBLE)
    chooseXButton.get.setVisibility(View.VISIBLE)
    chooseOButton.get.setVisibility(View.VISIBLE)

    maxTimeText.get.setVisibility(View.VISIBLE)
    maxTimeSpinner.get.setVisibility(View.VISIBLE)
    maxDepthText.get.setVisibility(View.VISIBLE)
    maxDepthSpinner.get.setVisibility(View.VISIBLE)
    adaptiveDepthSwitch.get.setVisibility(View.VISIBLE)
    positionsChoosingText.get.setVisibility(View.VISIBLE)
    positionsChoosingSpinner.get.setVisibility(View.VISIBLE)
    bestMoveHeuristicsText.get.setVisibility(View.VISIBLE)
    bestMoveHeuristicsSpinner.get.setVisibility(View.VISIBLE)
    randomizeDecisionsSwitch.get.setVisibility(View.VISIBLE)
  }

  def closeChooser(): Unit = {
    log("closing chooser")
    chooseSymbolLayer.get.setVisibility(View.GONE)

    //make sure that all buttons are gone, android gets crazy sometimes
    chooseSymbolText.get.setVisibility(View.GONE)
    chooseXButton.get.setVisibility(View.GONE)
    chooseOButton.get.setVisibility(View.GONE)

    maxTimeText.get.setVisibility(View.GONE)
    maxTimeSpinner.get.setVisibility(View.GONE)
    maxDepthText.get.setVisibility(View.GONE)
    maxDepthSpinner.get.setVisibility(View.GONE)
    adaptiveDepthSwitch.get.setVisibility(View.GONE)
    positionsChoosingText.get.setVisibility(View.GONE)
    positionsChoosingSpinner.get.setVisibility(View.GONE)
    bestMoveHeuristicsText.get.setVisibility(View.GONE)
    bestMoveHeuristicsSpinner.get.setVisibility(View.GONE)
    randomizeDecisionsSwitch.get.setVisibility(View.GONE)
  }

  def showBotTesting(): Unit = {
    log("showing bot testing")
    botTestingLayer.get.setVisibility(View.VISIBLE)

    botEnableSwitch.get.setVisibility(View.VISIBLE)
    botStatusText.get.setVisibility(View.VISIBLE)
  }

  def closeBotTesting(): Unit = {
    log("closing bot testing")
    botTestingLayer.get.setVisibility(View.GONE)

    botEnableSwitch.get.setVisibility(View.GONE)
    botStatusText.get.setVisibility(View.GONE)
  }

  /**
   * Starts new game with the same options
   */
  private[this] def onPlayAgain(v: View): Unit = {
    log("Intending to play again")
    var itnt = intent[GameActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
    itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
    game.gameType match {
      case Game.STANDARD =>
        itnt putExtra("TYPE", Game.STANDARD.toString)
      case Game.AI =>
        itnt putExtra("TYPE", Game.AI.toString)
        itnt putExtra("AI_HUMAN_SYMBOL", game.asInstanceOf[AIGame].getHuman.toString)
//      case Game.NETWORK =>
//        itnt = intent[StartNetworkGameActivity]
//        itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
//        itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
      case _ =>
        error("bad game type")
        return
    }
    finish()
    itnt start()
  }

  private[this] def onReplay(v: View): Unit = {
    onCloseMenu(v)
    closeBotTesting()
    replayGame()
    view.startReplaying()
  }

  private[this] def onBackToMainMenu(v: View) = {
    finish()
  }

  private[this] def setupBot(): Unit = {
    val g = game.asInstanceOf[AIGame]
    g.setMaxTime(maxTimeSpinner.get.getSelectedItem.toString.toInt)
    g.setMaxDepth(maxDepthSpinner.get.getSelectedItem.toString.toInt)
    g.setAdaptiveDepth(adaptiveDepthSwitch.get.isChecked)
    g.setPositionsChoosing(PositionsChoosing.withName(positionsChoosingSpinner.get.getSelectedItem.toString))
    g.setBestMoveHeuristics(BestMoveHeuristics.withName(bestMoveHeuristicsSpinner.get.getSelectedItem.toString))
    g.setRandomizeDecisions(randomizeDecisionsSwitch.get.isChecked)

    def displayStatus(s: String): Unit = {
      runOnUiThread(new Runnable() {
        override def run(): Unit = {
          botStatusText.get.setText(s)
        }
      })
    }
    g.setDisplayStatus(displayStatus)
  }

  private[this] def onPlayWithBotAsX(v: View) = {
    game.asInstanceOf[AIGame].setHumanSymbol(Player.X)
    setupBot()
    view.startGame()
    closeChooser()
  }

  private[this] def onPlayWithBotAsO(v: View) = {
    game.asInstanceOf[AIGame].setHumanSymbol(Player.O)
    setupBot()
    view.startGame()
    closeChooser()
  }

  private[this] def onBotEnabledCheck(buttonView: CompoundButton, isChecked: Boolean): Unit = {
    game.asInstanceOf[AIGame].setEnabled(isChecked)
  }
}
