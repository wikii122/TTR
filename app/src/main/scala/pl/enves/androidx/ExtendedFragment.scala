package pl.enves.androidx

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import pl.enves.ttr.R
import pl.enves.ttr.utils.themes.Theme

abstract class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected val fontPath = "fonts/comfortaa.ttf"

  protected var number = 0

  protected val layout: Int

  protected def changeFont(view: View, id: Int, typeface: Typeface): Unit = {
    val textView = find[TextView](view, id)
    textView.setTypeface(typeface)
  }

  protected def changeText(view: View, id: Int, textId: Int): Unit = {
    val textView = find[TextView](view, id)
    textView.setText(textId)
  }

  protected def changeText(view: View, id: Int, text: String): Unit = {
    val textView = find[TextView](view, id)
    textView.setText(text)
  }

  protected def getSavedTheme(prefs: SharedPreferences): Theme = {
    val defaultTheme = Theme(getResources, R.array.theme_five)
    return Theme(prefs.getString("THEME", defaultTheme.toJsonObject.toString))
  }

  override def onStart() {
    super.onStart()

    number = getArguments.getInt("NUMBER", 0)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(layout, container, false)
    return view
  }

  def onSelected(): Unit = {}

  def onDeSelected(): Unit = {}
}
