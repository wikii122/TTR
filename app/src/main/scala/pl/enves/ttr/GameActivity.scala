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
import pl.enves.ttr.utils.Configuration
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme

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
  private[this] var backToMainButton: Option[ImageButton] = None

  private[this] var chooseSymbolLayer: Option[View] = None
  private[this] var chooseSymbolText: Option[TextView] = None
  private[this] var chooseXButton: Option[ImageButton] = None
  private[this] var chooseOButton: Option[ImageButton] = None

  private[this] var difficultyText: Option[TextView] = None
  private[this] var difficultySeekBar: Option[SeekBar] = None

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

    setContentView(frameLayout)

    playAgainButton = Some((find[Button](R.id.button_play_again), find[Button](R.id.button_play_again_prompt)))
    gameCourseButton = Some((find[Button](R.id.button_game_course), find[Button](R.id.button_game_course_prompt)))
    contemplateButton = Some((find[Button](R.id.button_contemplate), find[Button](R.id.button_contemplate_prompt)))
    backToMainButton = Some(find[ImageButton](R.id.button_back_to_main))

    playAgainButton.get onClick onPlayAgain
    gameCourseButton.get onClick onReplay
    contemplateButton.get onClick onCloseMenu
    backToMainButton.get onClick onBackToMainMenu

    chooseSymbolText = Some(find[TextView](R.id.text_choose_symbol))
    chooseXButton = Some(find[ImageButton](R.id.button_symbol_X))
    chooseOButton = Some(find[ImageButton](R.id.button_symbol_O))

    chooseXButton.get onClick onPlayWithBotAsX
    chooseOButton.get onClick onPlayWithBotAsO

    difficultySeekBar = Some(find[SeekBar](R.id.seekBar_difficulty))
    difficultyText = Some(find[TextView](R.id.text_difficulty))

    if (game.gameType == Game.AI) {
      if (game.asInstanceOf[AIGame].getHuman.isEmpty) {
        showChooser()
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

    difficultyText.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    view.setTheme(theme)

    afterGameMenuLayer.get.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    playAgainButton.get.setTextColor(theme.color1, theme.color2)
    gameCourseButton.get.setTextColor(theme.color1, theme.color2)
    contemplateButton.get.setTextColor(theme.color1, theme.color2)
    backToMainButton.get.setColorMask(theme.background, theme.background, theme.color1)

    chooseSymbolLayer.get.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    chooseSymbolText.get.setTextColor(theme.color2)
    chooseXButton.get.setColorMask(theme.background, theme.background, theme.color1)
    chooseOButton.get.setColorMask(theme.background, theme.background, theme.color1)

    difficultyText.get.setTextColor(theme.color2)
    difficultySeekBar.get.setColors(theme.color1, theme.color2)
  }

  override def onPause(): Unit = {
    log("Pausing")

    super.onPause()
    view.onPause()

    if (game.gameType == Game.REPLAY) {
      game.asInstanceOf[ReplayGame].stopReplaying()
    }

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

    difficultyText.get.setVisibility(View.VISIBLE)
    difficultySeekBar.get.setVisibility(View.VISIBLE)

    difficultySeekBar.get.setProgress(Configuration.botDifficulty)
  }

  def closeChooser(): Unit = {
    log("closing chooser")
    chooseSymbolLayer.get.setVisibility(View.GONE)

    //make sure that all buttons are gone, android gets crazy sometimes
    chooseSymbolText.get.setVisibility(View.GONE)
    chooseXButton.get.setVisibility(View.GONE)
    chooseOButton.get.setVisibility(View.GONE)

    difficultyText.get.setVisibility(View.GONE)
    difficultySeekBar.get.setVisibility(View.GONE)
  }

  /**
   * Starts new game with the same options
   */
  private[this] def onPlayAgain(v: View): Unit = {
    log("Intending to play again")
    def restartGame(gameType: Game.Value): Unit = {
      val itnt = intent[GameActivity]
      itnt addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
      itnt addFlags Intent.FLAG_ACTIVITY_SINGLE_TOP
      game.gameType match {
        case Game.STANDARD =>
          itnt putExtra("TYPE", Game.STANDARD.toString)
        case Game.AI =>
          itnt putExtra("TYPE", Game.AI.toString)
        case Game.GPS_MULTIPLAYER => //TODO
        case _ =>
          error("bad game type")
          return
      }
      finish()
      itnt start()
    }

    if (game.gameType == Game.REPLAY) {
      restartGame(game.asInstanceOf[ReplayGame].getReplayedGameType)
    } else {
      restartGame(game.gameType)
    }
  }

  private[this] def onReplay(v: View): Unit = {
    onCloseMenu(v)
    replayGame()
  }

  private[this] def onBackToMainMenu(v: View) = {
    finish()
  }

  private[this] def setupBot(): Unit = {
    val g = game.asInstanceOf[AIGame]

    val difficulty = difficultySeekBar.get.getProgress
    g.setMaxTime((difficulty + 1) * 1000)

    if (difficulty != Configuration.botDifficulty) {
      Configuration.botDifficulty = difficulty
    }
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
}
