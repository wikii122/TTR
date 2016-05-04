package pl.enves.ttr.utils.start

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.Button
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorUiTweaks
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.GameState
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.{R, StartGameActivity}

class OfflineMenuFragment extends StyledFragment with Logging with ColorUiTweaks {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_offline_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val newStandardButton = (find[Button] (view, R.id.button_create_standard), find[Button](view, R.id.button_create_standard_prompt))
    val newBotGameButton = (find[Button] (view, R.id.button_create_bot), find[Button](view, R.id.button_create_bot_prompt))
    val continueGameButton = (find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt))

    GameState.onDataChanged(enableButtons)
    enableButtons()

    newStandardButton onClick onStandardGame
    newBotGameButton onClick onBotGame
    continueGameButton onClick continueGame
  }

  private[this] def onStandardGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startStandardGame()
  }

  private[this] def onBotGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startBotGame()
  }

  private[this] def continueGame(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].continueGame()

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val newStandardButton = (find[Button] (view, R.id.button_create_standard), find[Button](view, R.id.button_create_standard_prompt))
    val newBotGameButton = (find[Button] (view, R.id.button_create_bot), find[Button](view, R.id.button_create_bot_prompt))
    val continueGameButton = (find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt))

    newStandardButton.setTypeface(typeface)
    newBotGameButton.setTypeface(typeface)
    continueGameButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val newStandardButton = (find[Button] (view, R.id.button_create_standard), find[Button](view, R.id.button_create_standard_prompt))
    val newBotGameButton = (find[Button] (view, R.id.button_create_bot), find[Button](view, R.id.button_create_bot_prompt))
    val continueGameButton = (find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt))

    newStandardButton.setTextColor(theme.color1, theme.color2)
    newBotGameButton.setTextColor(theme.color1, theme.color2)
    continueGameButton.setTextColor(theme.color1, theme.color2)
    continueGameButton.setTextColor(colorStateList(theme.color1, 0.25f), colorStateList(theme.color2, 0.25f))
  }

  def enableButtons(): Unit = runOnMainThread {
    setContinueButtonEnabled(GameState.active)
  }

  private[this] def setContinueButtonEnabled(enabled: Boolean): Unit = {
    val view = getView
    if (view != null) {
      val continueGameButton = Some((find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt)))

      if (enabled) {
        continueGameButton.get.enable()
      } else {
        continueGameButton.get.disable()
      }
    }
  }
}
