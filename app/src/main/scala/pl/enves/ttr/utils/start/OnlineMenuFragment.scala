package pl.enves.ttr.utils.start

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{Button, RelativeLayout}
import pl.enves.androidx.Logging
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.Code
import pl.enves.ttr.utils.ExecutorContext._
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.{R, StartGameActivity}

import scala.concurrent.Future

class OnlineMenuFragment extends StyledFragment with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_online_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val invitationsButton = (find[Button](view, R.id.button_activity), find[Button](view, R.id.button_activity_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    setInvitationsNumber()

    invitationsButton onClick listInvitations
    newNetworkButton onClick onNetworkGame
  }

  private[this] def listInvitations(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].startNetworkGame(Code.INVITATION)

  private[this] def onNetworkGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startNetworkGame(Code.PLAYERS)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val invitationsButton = (find[Button](view, R.id.button_activity), find[Button](view, R.id.button_activity_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    invitationsButton.setTypeface(typeface)
    newNetworkButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val invitationsButton = (find[Button](view, R.id.button_activity), find[Button](view, R.id.button_activity_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    invitationsButton.setTextColor(theme.color1, theme.color2)
    newNetworkButton.setTextColor(theme.color1, theme.color2)
  }

  def onConnected() = {
    if (isVisible) {
      setInvitationsNumber()
    }
  }

  private[this] def setInvitationsNumber(): Unit = {
    log("Requesting invitation list")
    setInvitationsNumber(0)

    Future.sequence(PlayServices.invitations :: PlayServices.myGames :: Nil) onSuccess {
      case list => setInvitationsNumber(list map (_.length) sum)
    }
  }

  private[this] def setInvitationsNumber(count: Int): Unit = runOnMainThread {
    val view = Option(getView)
    if (view.isDefined) {
      val invitationsLayout = find[RelativeLayout](view.get, R.id.layout_activity)
      val invitationsButton = find[Button](view.get, R.id.button_activity)
      val text = getActivity.getResources.getText(R.string.activity).toString
      invitationsLayout setVisibility View.VISIBLE
      invitationsButton setText text.format(count)
    }
  }
}
