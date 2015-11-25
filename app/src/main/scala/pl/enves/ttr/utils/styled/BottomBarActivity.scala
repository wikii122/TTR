package pl.enves.ttr.utils.styled

import android.graphics.Typeface
import android.view.WindowManager
import pl.enves.androidx.ExtendedActivity
import pl.enves.ttr.utils.themes.Theme

class BottomBarActivity extends StyledActivity {
  override def setGui() = {
    val window = getWindow
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)
  }
}
