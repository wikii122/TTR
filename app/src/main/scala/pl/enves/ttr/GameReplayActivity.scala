package pl.enves.ttr

import android.graphics.Typeface
import android.os.Bundle
import pl.enves.ttr.graphics.GameView
import pl.enves.ttr.logic.GameManager
import pl.enves.ttr.logic.games.ReplayGame
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme
import spray.json._

class GameReplayActivity extends StyledActivity with GameManager {
  private[this] lazy val view: GameView = GameView(this, close)

  override def onCreate(state: Bundle): Unit = {
    log("Creating")

    super.onCreate(state)

    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())
    val jsValue = b.getString("GAME_DATA").parseJson
    val showEnd = b.getBoolean("SHOW_END", false)
    game = ReplayGame(jsValue, showEnd)

    setContentView(view)
  }

  override def onResume(): Unit = {
    super.onResume()

    view.onResume()

    game.asInstanceOf[ReplayGame].startReplaying()
  }

  override def onPause(): Unit = {
    super.onPause()

    game.asInstanceOf[ReplayGame].stopReplaying()

    view.onPause()
  }

  override def onStop(): Unit = {
    super.onStop()

    //to be consistent with GameActivity behaviour
    finish()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)
    view.setTheme(theme)
  }

  def close(): Unit = {
    finish()
  }
}
