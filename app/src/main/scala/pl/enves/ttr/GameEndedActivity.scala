package pl.enves.ttr

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget._
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.JsonProtocol._
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.utils.{AdUtils, Configuration}
import spray.json._

class GameEndedActivity extends StyledActivity with AdUtils {

  private[this] var gameEndedText: Option[TextView] = None
  private[this] var playAgainButton: Option[(Button, Button)] = None
  private[this] var gameCourseButton: Option[(Button, Button)] = None
  private[this] var showGameEndButton: Option[(Button, Button)] = None
  private[this] var backButton: Option[ImageButton] = None

  override def onCreate(state: Bundle): Unit = {
    super.onCreate(state)

    setContentView(R.layout.game_ended_layout)

    gameEndedText = Some(find[TextView](R.id.text_game_ended))
    playAgainButton = Some((find[Button](R.id.button_play_again), find[Button](R.id.button_play_again_prompt)))
    gameCourseButton = Some((find[Button](R.id.button_replay_moves), find[Button](R.id.button_replay_moves_prompt)))
    showGameEndButton = Some((find[Button](R.id.button_show_game_end), find[Button](R.id.button_show_game_end_prompt)))
    backButton = Some(find[ImageButton](R.id.button_back_to_main))

    playAgainButton.get onClick onPlayAgain
    gameCourseButton.get onClick onReplay
    showGameEndButton.get onClick onShowGameEnd
    backButton.get onClick onBack

    if (!Configuration.isPaid) {
      loadAdToStub(R.id.stub_game_ended_ad)
    }
  }

  override def onStop(): Unit = {
    super.onStop()

    //user definitely knows that game has ended
    GameState clear()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    gameEndedText.get.setTypeface(typeface)
    playAgainButton.get.setTypeface(typeface)
    gameCourseButton.get.setTypeface(typeface)
    showGameEndButton.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    gameEndedText.get.setTextColor(theme.color2)
    playAgainButton.get.setTextColor(theme.color1, theme.color2)
    gameCourseButton.get.setTextColor(theme.color1, theme.color2)
    showGameEndButton.get.setTextColor(theme.color1, theme.color2)
    backButton.get.setColor(theme.color1)
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
        itnt.putExtra("TYPE", Game.STANDARD.toString)
      case Game.AI =>
        itnt.putExtra("TYPE", Game.AI.toString)
      case Game.GPS_MULTIPLAYER => //TODO
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
