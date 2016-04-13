package pl.enves.ttr.utils.styled

import android.graphics.Typeface
import pl.enves.androidx.views.ExtendedFragment
import pl.enves.ttr.utils.Configuration
import pl.enves.ttr.utils.themes.Theme

abstract class StyledFragment extends ExtendedFragment with Styled {
  override def onStart() {
    super.onStart()

    val typeface = Typeface.createFromAsset(getContext.getAssets, Configuration.defaultTypefacePath)
    setTypeface(typeface)

    val theme = Configuration.pickedTheme
    setColorTheme(theme)
  }

  override def setTypeface(typeface: Typeface): Unit = {}

  override def setColorTheme(theme: Theme): Unit = {}
}
