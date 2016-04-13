package pl.enves.ttr.utils.styled

import android.graphics.Typeface
import android.view.{View, WindowManager}
import pl.enves.androidx.views.ExtendedActivity
import pl.enves.ttr.utils.Configuration
import pl.enves.ttr.utils.themes.Theme

abstract class StyledActivity extends ExtendedActivity with Styled {
  override def onStart() = {
    super.onStart()

    setGui()

    val typeface = Typeface.createFromAsset(getAssets, Configuration.defaultTypefacePath)
    setTypeface(typeface)

    val theme = Configuration.pickedTheme
    setColorTheme(theme)
  }

  override def setTypeface(typeface: Typeface): Unit = {}

  override def setColorTheme(theme: Theme): Unit = {
    getWindow.getDecorView.setBackgroundColor(theme.background)
  }

  protected def setGui(): Unit = {
    val window = getWindow
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    getWindow.getDecorView.setSystemUiVisibility(
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )
  }
}
