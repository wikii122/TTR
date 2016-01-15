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

  private[this] var chooseSymbolLayer: Option[View] = None
  private[this] var chooseSymbolText: Option[TextView] = None
  private[this] var chooseXButton: Option[ImageButton] = None
  private[this] var chooseOButton: Option[ImageButton] = None

  private[this] var difficultyText: Option[TextView] = None
  private[this] var difficultyNumber: Option[TextView] = None
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

    chooseSymbolText = Some(find[TextView](R.id.text_choose_symbol))
    chooseXButton = Some(find[ImageButton](R.id.button_symbol_X))
    chooseOButton = Some(find[ImageButton](R.id.button_symbol_O))

    chooseXButton.get onClick onPlayWithBotAsX
    chooseOButton.get onClick onPlayWithBotAsO

    difficultySeekBar = Some(find[SeekBar](R.id.seekBar_difficulty))
    difficultyText = Some(find[TextView](R.id.text_difficulty))
    difficultyNumber = Some(find[TextView](R.id.text_difficulty_number))

    difficultySeekBar.get onChange onDifficultyChanged

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

    chooseSymbolText.get.setTypeface(typeface)

    difficultyText.get.setTypeface(typeface)
    difficultyNumber.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)
    view.setTheme(theme)

    afterGameMenuLayer.get.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    chooseSymbolLayer.get.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    chooseSymbolText.get.setTextColor(theme.color2)
    chooseXButton.get.setColorMask(theme.background, theme.background, theme.color1)
    chooseOButton.get.setColorMask(theme.background, theme.background, theme.color1)

    difficultyText.get.setTextColor(theme.color2)
    difficultyNumber.get.setTextColor(theme.color1)
    difficultySeekBar.get.setColors(theme.color1, theme.color2)
  }

  override def onPause(): Unit = {
    log("Pausing")

    super.onPause()
    view.onPause()
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
    if (game.canBeSaved) {
      GameState store game
    } else {
      GameState clear()
    }

    //if game was saved, user can select to continue it in main menu
    finish()
  }

  def showMenu(): Unit = {
    val itnt = intent[GameEndedActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    itnt.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    itnt.putExtra("GAME_DATA", game.toJson.compactPrint)
    finish()
    itnt.start()
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
    difficultyNumber.get.setVisibility(View.VISIBLE)

    val difficulty = Configuration.botDifficulty
    difficultySeekBar.get.setProgress(difficulty)
    if(difficulty == 0) {
      //seekBar' default progress is 0, so there is no change informed to ProgressChangeListener
      onDifficultyChanged(difficultySeekBar.get, difficulty, false)
    }
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
    difficultyNumber.get.setVisibility(View.GONE)
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

  private[this] def onDifficultyChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit = {
    difficultyNumber.get.setText((progress + 1).toString)
  }
}
