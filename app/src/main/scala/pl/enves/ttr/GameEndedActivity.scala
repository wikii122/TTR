package pl.enves.ttr

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget._
import com.google.android.gms.ads.AdView
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.{Code, AdUtils}
import pl.enves.ttr.utils.JsonProtocol._
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme
import spray.json._

class GameEndedActivity extends StyledActivity with AdUtils {

  override def onCreate(state: Bundle): Unit = {
    super.onCreate(state)

    setContentView(R.layout.game_ended_layout)

    val playAgainButton = (find[Button](R.id.button_play_again), find[Button](R.id.button_play_again_prompt))
    val gameCourseButton = (find[Button](R.id.button_replay_moves), find[Button](R.id.button_replay_moves_prompt))
    val showGameEndButton = (find[Button](R.id.button_show_game_end), find[Button](R.id.button_show_game_end_prompt))
    val backButton = find[ImageButton](R.id.button_back)

    playAgainButton onClick onPlayAgain
    gameCourseButton onClick onReplay
    showGameEndButton onClick onShowGameEnd
    backButton onClick onBack

    val adView = find[AdView](R.id.ad_view_game_ended)
    loadAd(adView)
  }

  override def onResume(): Unit = {
    super.onResume()

    val adView = find[AdView](R.id.ad_view_game_ended)
    adView.resume()
  }

  override def onPause(): Unit = {
    super.onPause()

    val adView = find[AdView](R.id.ad_view_game_ended)
    adView.pause()
  }

  override def onStop(): Unit = {
    super.onStop()

    //user definitely knows that game has ended
    GameState clear()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()

    val adView = find[AdView](R.id.ad_view_game_ended)
    adView.destroy()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    val gameEndedText = find[TextView](R.id.text_game_ended)
    val playAgainButton = (find[Button](R.id.button_play_again), find[Button](R.id.button_play_again_prompt))
    val gameCourseButton = (find[Button](R.id.button_replay_moves), find[Button](R.id.button_replay_moves_prompt))
    val showGameEndButton = (find[Button](R.id.button_show_game_end), find[Button](R.id.button_show_game_end_prompt))

    gameEndedText.setTypeface(typeface)
    playAgainButton.setTypeface(typeface)
    gameCourseButton.setTypeface(typeface)
    showGameEndButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    val gameEndedText = find[TextView](R.id.text_game_ended)
    val playAgainButton = (find[Button](R.id.button_play_again), find[Button](R.id.button_play_again_prompt))
    val gameCourseButton = (find[Button](R.id.button_replay_moves), find[Button](R.id.button_replay_moves_prompt))
    val showGameEndButton = (find[Button](R.id.button_show_game_end), find[Button](R.id.button_show_game_end_prompt))
    val backButton = find[ImageButton](R.id.button_back)

    gameEndedText.setTextColor(theme.color2)
    playAgainButton.setTextColor(theme.color1, theme.color2)
    gameCourseButton.setTextColor(theme.color1, theme.color2)
    showGameEndButton.setTextColor(theme.color1, theme.color2)
    backButton.setColor(theme.color2)
  }

  /**
   * Starts new game with the same options
   */
  private[this] def onPlayAgain(v: View): Unit = {
    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())
    val jsValue = b.getString("GAME_DATA").parseJson

    val itnt = intent[GameActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    itnt.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    jsValue.asJsObject.fields("type").convertTo[Game.Value] match {
      case Game.STANDARD =>
        itnt.putExtra(Code.TYPE, Game.STANDARD.toString)
      case Game.BOT =>
        itnt.putExtra(Code.TYPE, Game.BOT.toString)
      case Game.GPS_MULTIPLAYER =>
        itnt.putExtra(Code.TYPE, Game.GPS_MULTIPLAYER.toString)
        itnt.putExtra(Code.DATA, Code.REMATCH)
        itnt.putExtra(Code.REMATCH, b.getString(Code.REMATCH))
      case _ =>
        error("bad game type")
        return
    }
    log("Intending to play again")
    finish()
    itnt.start()
  }

  private[this] def onReplay(v: View): Unit = {
    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())

    val itnt = intent[GameReplayActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    itnt.putExtra("GAME_DATA", b.getString("GAME_DATA"))

    log("Intending to replay")
    itnt.start()
  }

  private[this] def onShowGameEnd(v: View): Unit = {
    val b: Bundle = Option(getIntent.getExtras) getOrElse (throw new UninitializedError())

    val itnt = intent[GameReplayActivity]
    itnt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    itnt.putExtra("GAME_DATA", b.getString("GAME_DATA"))
    itnt.putExtra("SHOW_END", true)

    log("Intending to replay end")
    itnt.start()
  }

  private[this] def onBack(v: View) = {
    finish()
  }
}
