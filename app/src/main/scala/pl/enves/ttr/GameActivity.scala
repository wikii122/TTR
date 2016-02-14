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

  private[this] lazy val botGameSetupLayer = getLayoutInflater.inflate(R.layout.bot_game_setup_layout, null)

  override def onCreate(state: Bundle): Unit = {
    log("Creating")

    super.onCreate(state)

    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())
    Game withName (b getString "TYPE") match {
      case Game.STANDARD =>
        game = Game.create(Game.STANDARD)
        view.startGame()
      case Game.BOT =>
        game = Game.create(Game.BOT)
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

    botGameSetupLayer.setVisibility(View.GONE)

    val botGameSetupLayoutParams = new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
      Gravity.TOP)
    frameLayout.addView(botGameSetupLayer, botGameSetupLayoutParams)

    setContentView(frameLayout)

    val chooseXButton = find[ImageButton](R.id.button_symbol_X)
    val chooseOButton = find[ImageButton](R.id.button_symbol_O)

    chooseXButton onClick onPlayWithBotAsX
    chooseOButton onClick onPlayWithBotAsO

    val difficultySeekBar = find[SeekBar](R.id.seekBar_difficulty)

    difficultySeekBar onChange onDifficultyChanged

    if (game.gameType == Game.BOT) {
      if (game.asInstanceOf[BotGame].getHuman.isEmpty) {
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

    val chooseSymbolText = find[TextView](R.id.text_choose_symbol)

    val difficultyText = find[TextView](R.id.text_difficulty)
    val difficultyNumber = find[TextView](R.id.text_difficulty_number)

    chooseSymbolText.setTypeface(typeface)

    difficultyText.setTypeface(typeface)
    difficultyNumber.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)
    view.setTheme(theme)

    val chooseSymbolText = find[TextView](R.id.text_choose_symbol)
    val chooseXButton = find[ImageButton](R.id.button_symbol_X)
    val chooseOButton = find[ImageButton](R.id.button_symbol_O)

    val difficultySeekBar = find[SeekBar](R.id.seekBar_difficulty)
    val difficultyText = find[TextView](R.id.text_difficulty)
    val difficultyNumber = find[TextView](R.id.text_difficulty_number)

    botGameSetupLayer.setBackgroundColor(colorTransparent(theme.background, 0.8f))

    chooseSymbolText.setTextColor(theme.color2)

    chooseXButton.setColor(theme.color1)
    chooseOButton.setColor(theme.color1)

    difficultyText.setTextColor(theme.color2)
    difficultyNumber.setTextColor(theme.color1)
    difficultySeekBar.setColors(theme.color1, theme.color2)
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

    val chooseSymbolText = find[TextView](R.id.text_choose_symbol)
    val chooseXButton = find[ImageButton](R.id.button_symbol_X)
    val chooseOButton = find[ImageButton](R.id.button_symbol_O)

    val difficultySeekBar = find[SeekBar](R.id.seekBar_difficulty)
    val difficultyText = find[TextView](R.id.text_difficulty)
    val difficultyNumber = find[TextView](R.id.text_difficulty_number)

    botGameSetupLayer.setVisibility(View.VISIBLE)

    //make sure that all buttons are visible, android gets crazy sometimes
    chooseSymbolText.setVisibility(View.VISIBLE)
    chooseXButton.setVisibility(View.VISIBLE)
    chooseOButton.setVisibility(View.VISIBLE)

    difficultyText.setVisibility(View.VISIBLE)
    difficultySeekBar.setVisibility(View.VISIBLE)
    difficultyNumber.setVisibility(View.VISIBLE)

    val difficulty = Configuration.botDifficulty
    difficultySeekBar.setProgress(difficulty)
    if (difficulty == 0) {
      //seekBar' default progress is 0, so there is no change informed to ProgressChangeListener
      onDifficultyChanged(difficultySeekBar, difficulty, false)
    }
  }

  def closeChooser(): Unit = {
    log("closing chooser")

    val chooseSymbolText = find[TextView](R.id.text_choose_symbol)
    val chooseXButton = find[ImageButton](R.id.button_symbol_X)
    val chooseOButton = find[ImageButton](R.id.button_symbol_O)

    val difficultySeekBar = find[SeekBar](R.id.seekBar_difficulty)
    val difficultyText = find[TextView](R.id.text_difficulty)
    val difficultyNumber = find[TextView](R.id.text_difficulty_number)

    botGameSetupLayer.setVisibility(View.GONE)

    //make sure that all buttons are gone, android gets crazy sometimes
    chooseSymbolText.setVisibility(View.GONE)
    chooseXButton.setVisibility(View.GONE)
    chooseOButton.setVisibility(View.GONE)

    difficultyText.setVisibility(View.GONE)
    difficultySeekBar.setVisibility(View.GONE)
    difficultyNumber.setVisibility(View.GONE)
  }

  private[this] def setupBot(): Unit = {
    val g = game.asInstanceOf[BotGame]

    val difficultySeekBar = find[SeekBar](R.id.seekBar_difficulty)

    val difficulty = difficultySeekBar.getProgress
    g.setMaxTime((difficulty + 1) * 1000)

    if (difficulty != Configuration.botDifficulty) {
      Configuration.botDifficulty = difficulty
    }
  }

  private[this] def onPlayWithBotAsX(v: View) = {
    game.asInstanceOf[BotGame].setHumanSymbol(Player.X)
    setupBot()
    view.startGame()
    closeChooser()
  }

  private[this] def onPlayWithBotAsO(v: View) = {
    game.asInstanceOf[BotGame].setHumanSymbol(Player.O)
    setupBot()
    view.startGame()
    closeChooser()
  }

  private[this] def onDifficultyChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit = {
    val difficultyNumber = find[TextView](R.id.text_difficulty_number)

    difficultyNumber.setText((progress + 1).toString)
  }
}
