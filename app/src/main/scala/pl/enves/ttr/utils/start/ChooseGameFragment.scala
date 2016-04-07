package pl.enves.ttr.utils.start

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{Button, TextView}
import pl.enves.androidx.Logging
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.Code
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.{R, StartGameActivity}

class ChooseGameFragment extends StyledFragment with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_new_game_menu, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val newStandardButton = (find[Button] (view, R.id.button_create_standard), find[Button](view, R.id.button_create_standard_prompt))
    val newBotGameButton = (find[Button] (view, R.id.button_create_bot), find[Button](view, R.id.button_create_bot_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))

    newStandardButton onClick onStandardGame
    newBotGameButton onClick onBotGame
    newNetworkButton onClick onNetworkGame
  }

  private[this] def onStandardGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startStandardGame()
  }

  private[this] def onBotGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startBotGame()
  }

  private[this] def onNetworkGame(v: View) = {
    getActivity.asInstanceOf[StartGameActivity].startNetworkGame(Code.PLAYERS)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    val view = getView
    val newStandardButton = (find[Button] (view, R.id.button_create_standard), find[Button](view, R.id.button_create_standard_prompt))
    val newBotGameButton = (find[Button] (view, R.id.button_create_bot), find[Button](view, R.id.button_create_bot_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))
    val gameTypeText = find[TextView](view, R.id.text_game_type)

    newStandardButton.setTypeface(typeface)
    newBotGameButton.setTypeface(typeface)
    newNetworkButton.setTypeface(typeface)
    gameTypeText.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val newStandardButton = (find[Button] (view, R.id.button_create_standard), find[Button](view, R.id.button_create_standard_prompt))
    val newBotGameButton = (find[Button] (view, R.id.button_create_bot), find[Button](view, R.id.button_create_bot_prompt))
    val newNetworkButton = (find[Button] (view, R.id.button_create_network), find[Button](view, R.id.button_create_network_prompt))
    val gameTypeText = find[TextView](view, R.id.text_game_type)

    newStandardButton.setTextColor(theme.color1, theme.color2)
    newBotGameButton.setTextColor(theme.color1, theme.color2)
    newNetworkButton.setTextColor(theme.color1, theme.color2)
    gameTypeText.setTextColor(theme.color2)

  }
}
