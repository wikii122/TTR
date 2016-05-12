package pl.enves.ttr
package utils
package start


import android.graphics.{PorterDuff, Typeface}
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{Button, RelativeLayout}
import pl.enves.androidx.Logging
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.ExecutorContext._
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme

import scala.concurrent.Future

class MainMenuFragment extends StyledFragment with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_main_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val onlineButton = (find[Button](view, R.id.button_online), find[Button](view, R.id.button_online_prompt))
    val onlineActivityButton = find[Button](view, R.id.button_online_activity)
    val offlineButton = (find[Button](view, R.id.button_offline), find[Button](view, R.id.button_offline_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    onlineButton onClick startOnlineMenu
    onlineActivityButton onClick listInvitations
    offlineButton onClick startOfflineMenu
    settingsButton onClick showSettings

    onConnected()
  }

  private[this] def listInvitations(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].startNetworkGame(Code.INVITATION)

  private[this] def startOnlineMenu(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].showOnlineMenu()

  private[this] def startOfflineMenu(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].showOfflineMenu()

  private[this] def showSettings(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].launchSettings()

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val onlineButton = (find[Button](view, R.id.button_online), find[Button](view, R.id.button_online_prompt))
    val onlineActivityButton = find[Button](view, R.id.button_online_activity)
    val offlineButton = (find[Button](view, R.id.button_offline), find[Button](view, R.id.button_offline_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    onlineButton.setTypeface(typeface)
    onlineActivityButton.setTypeface(typeface)
    offlineButton.setTypeface(typeface)
    settingsButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val onlineButton = (find[Button](view, R.id.button_online), find[Button](view, R.id.button_online_prompt))
    val onlineActivityButton = find[Button](view, R.id.button_online_activity)
    val offlineButton = (find[Button](view, R.id.button_offline), find[Button](view, R.id.button_offline_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    onlineButton.setTextColor(theme.color1, theme.color2)
    onlineActivityButton.getBackground.setColorFilter(theme.color1, PorterDuff.Mode.SRC_IN)
    onlineActivityButton.setTextColor(theme.background)

    offlineButton.setTextColor(theme.color1, theme.color2)
    settingsButton.setTextColor(theme.color1, theme.color2)
  }

  def onConnected() = {
    val view = Option(getView)
    if (view.isDefined) {
      val onlineMenuEntry = find[RelativeLayout](view.get, R.id.layout_online_menu_entry)

      if (PlayServices.isConnected) {
        onlineMenuEntry setVisibility View.VISIBLE
        setActivityCount()
      } else {
        onlineMenuEntry setVisibility View.GONE
      }
    }
  }

  private[this] def setActivityCount(): Unit = {
    log("Requesting activity list")
    setActivityCount(0)

    Future.sequence(PlayServices.invitations :: PlayServices.myGames :: Nil) onSuccess {
      case list => setActivityCount(list map (_.length) sum)
    }
  }

  private[this] def setActivityCount(count: Int): Unit = runOnMainThread {
    val view = Option(getView)
    if (view.isDefined) {
      val onlineActivityButton = find[Button](view.get, R.id.button_online_activity)
      if (count != 0) {
        onlineActivityButton setVisibility View.VISIBLE
        onlineActivityButton setText count.toString
      } else {
        onlineActivityButton setVisibility View.GONE
      }
    }
  }
}
