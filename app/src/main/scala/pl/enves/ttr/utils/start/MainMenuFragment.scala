package pl.enves.ttr.utils.start

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{Button, RelativeLayout}
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorUiTweaks
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.GameState
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.{R, StartGameActivity}

class MainMenuFragment extends StyledFragment with ColorUiTweaks with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_main_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val invitationsButton = (find[Button](view, R.id.button_invitations), find[Button](view, R.id.button_invitations_prompt))
    val newGameButton = (find[Button](view, R.id.button_new), find[Button](view, R.id.button_new_prompt))
    val continueGameButton = (find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    setContinueButtonEnabled(GameState.active)
    setInvitationsNumber(0)

    invitationsButton onClick onInvitations
    newGameButton onClick onNewGame
    continueGameButton onClick onContinue
    settingsButton onClick onSettings
  }

  private[this] def onInvitations(v: View): Unit = {
    //TODO
  }

  private[this] def onNewGame(v: View): Unit = {
    getActivity.asInstanceOf[StartGameActivity].showNewGameMenu()
  }

  private[this] def onContinue(v: View): Unit = {
    getActivity.asInstanceOf[StartGameActivity].continueGame()
  }

  private[this] def onSettings(v: View): Unit = {
    getActivity.asInstanceOf[StartGameActivity].launchSettings()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val invitationsButton = (find[Button](view, R.id.button_invitations), find[Button](view, R.id.button_invitations_prompt))
    val newGameButton = (find[Button](view, R.id.button_new), find[Button](view, R.id.button_new_prompt))
    val continueGameButton = (find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    invitationsButton.setTypeface(typeface)
    newGameButton.setTypeface(typeface)
    continueGameButton.setTypeface(typeface)
    settingsButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val invitationsButton = (find[Button](view, R.id.button_invitations), find[Button](view, R.id.button_invitations_prompt))
    val newGameButton = (find[Button](view, R.id.button_new), find[Button](view, R.id.button_new_prompt))
    val continueGameButton = (find[Button](view, R.id.button_continue), find[Button](view, R.id.button_continue_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    invitationsButton.setTextColor(theme.color1, theme.color2)
    newGameButton.setTextColor(theme.color1, theme.color2)
    continueGameButton.setTextColor(colorStateList(theme.color1, 0.25f), colorStateList(theme.color2, 0.25f))
    settingsButton.setTextColor(theme.color1, theme.color2)
  }

  def setContinueButtonEnabled(enabled: Boolean): Unit = {
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

  def setInvitationsNumber(number: Int): Unit = {
    val view = getView
    if (view != null) {
      val invitationsLayout = find[RelativeLayout](view, R.id.layout_invitations)

      if (number != 0) {
        invitationsLayout.setVisibility(View.VISIBLE)
        val invitationsButton = find[Button](view, R.id.button_invitations)
        val text = getActivity.getResources.getText(R.string.invitations).toString
        invitationsButton.setText(text.format(number))
      } else {
        invitationsLayout.setVisibility(View.GONE)
      }
    }
  }
}