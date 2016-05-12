package pl.enves.ttr.utils.start

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.Button
import pl.enves.androidx.Logging
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.Code
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.{R, StartGameActivity}

class OnlineMenuFragment extends StyledFragment with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_online_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val activeButton = (find[Button](view, R.id.button_active), find[Button](view, R.id.button_active_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    activeButton onClick listInvitations
    newNetworkButton onClick startNetworkGame
  }

  private[this] def listInvitations(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].startNetworkGame(Code.INVITATION)

  private[this] def startNetworkGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startNetworkGame(Code.PLAYERS)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val activeButton = (find[Button](view, R.id.button_active), find[Button](view, R.id.button_active_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    activeButton.setTypeface(typeface)
    newNetworkButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val activeButton = (find[Button](view, R.id.button_active), find[Button](view, R.id.button_active_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    activeButton.setTextColor(theme.color1, theme.color2)
    newNetworkButton.setTextColor(theme.color1, theme.color2)
  }
}
