package pl.enves.ttr
package utils
package start


import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.Button
import pl.enves.androidx.Logging
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme

class MainMenuFragment extends StyledFragment with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_main_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val onlineButton = (find[Button](view, R.id.button_online), find[Button](view, R.id.button_online_prompt))
    val offlineButton = (find[Button](view, R.id.button_offline), find[Button](view, R.id.button_offline_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    onlineButton onClick startOnlineMenu
    offlineButton onClick startOfflineMenu
    settingsButton onClick showSettings
  }

  private[this] def startOnlineMenu(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].showOnlineMenu()

  private[this] def startOfflineMenu(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].showOfflineMenu()

  private[this] def showSettings(v: View): Unit =
    getActivity.asInstanceOf[StartGameActivity].launchSettings()

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val onlineButton = (find[Button](view, R.id.button_online), find[Button](view, R.id.button_online_prompt))
    val offlineButton = (find[Button](view, R.id.button_offline), find[Button](view, R.id.button_offline_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    onlineButton.setTypeface(typeface)
    offlineButton.setTypeface(typeface)
    settingsButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val onlineButton = (find[Button](view, R.id.button_online), find[Button](view, R.id.button_online_prompt))
    val offlineButton = (find[Button](view, R.id.button_offline), find[Button](view, R.id.button_offline_prompt))
    val settingsButton = (find[Button](view, R.id.button_settings), find[Button](view, R.id.button_settings_prompt))

    onlineButton.setTextColor(theme.color1, theme.color2)
    offlineButton.setTextColor(theme.color1, theme.color2)
    settingsButton.setTextColor(theme.color1, theme.color2)
  }
}
