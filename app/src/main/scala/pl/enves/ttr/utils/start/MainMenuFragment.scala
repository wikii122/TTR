package pl.enves.ttr
package utils
package start

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{Button, RelativeLayout}
import com.google.android.gms.games.multiplayer.Invitation
import pl.enves.androidx.Logging
import pl.enves.androidx.color.ColorUiTweaks
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.GameState
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.ExecutorContext._
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme

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
    setInvitationsNumber()

    invitationsButton onClick listInvitations(Nil)
    newGameButton onClick startNewGame
    continueGameButton onClick continueGame
    settingsButton onClick showSettings
  }

  private[this] def listInvitations(invitations: List[Invitation])(v: View): Unit = {
    val itnt = PlayServices.inboxIntent
    getActivity.startActivityForResult(itnt, Code.SELECT_INVITATIONS)
  }

  private[this] def startNewGame(v: View): Unit = {
    getActivity.asInstanceOf[StartGameActivity].showNewGameMenu()
  }

  private[this] def continueGame(v: View): Unit = {
    getActivity.asInstanceOf[StartGameActivity].continueGame()
  }

  private[this] def showSettings(v: View): Unit = {
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

  def onConnected() = {
    setInvitationsNumber()
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

  private[this] def setInvitationsNumber(): Unit = {
    log("Requesting invitation list")
    setInvitationsNumber(Nil)
    PlayServices.invitations.onSuccess {
      case list => {
        log(s"Received invitation list with ${list.length} invitations")
        runOnMainThread { setInvitationsNumber(list) }
      }
    }
  }

  private[this] def setInvitationsNumber(invitations: List[Invitation]): Unit = runOnMainThread {
    val view = Option(getView)
    if (view.isDefined) {
      val invitationsLayout = find[RelativeLayout](view.get, R.id.layout_invitations)
      val invitationsButton = find[Button](view.get, R.id.button_invitations)

      invitationsButton onClick listInvitations(invitations)

      if (invitations.nonEmpty) {
        val text = getActivity.getResources.getText(R.string.invitations).toString
        invitationsLayout.setVisibility(View.VISIBLE)
        invitationsButton.setText(text.format(invitations.length))
      } else {
        invitationsLayout.setVisibility(View.GONE)
      }
    }
  }
}
