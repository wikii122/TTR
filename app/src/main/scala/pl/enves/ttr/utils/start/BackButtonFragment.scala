package pl.enves.ttr.utils.start

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.ImageButton
import pl.enves.androidx.Logging
import pl.enves.androidx.helpers._
import pl.enves.ttr.R
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme

class BackButtonFragment extends StyledFragment with Logging {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_back_button, container, false)
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val view = getView
    val button = find[ImageButton](view, R.id.button_back)

    button onClick onBack
  }

  private[this] def onBack(v: View) = {
    getActivity.onBackPressed()
  }

  override def setColorTheme(theme: Theme): Unit = {
    val view = getView
    val button = find[ImageButton](view, R.id.button_back)

    button.setColor(theme.color2)
  }
}
