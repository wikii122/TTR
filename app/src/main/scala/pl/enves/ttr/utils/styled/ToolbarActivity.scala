package pl.enves.ttr.utils.styled

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.{MenuItem, WindowManager}
import pl.enves.androidx.helpers._
import pl.enves.ttr.R
import pl.enves.ttr.utils.themes.Theme

abstract class ToolbarActivity extends StyledActivity {
  protected var toolbar: Option[Toolbar] = None

  protected def setupToolbar(id: Int): Unit = {
    toolbar = Some(find[Toolbar](id))

    setSupportActionBar(toolbar.get)
  }

  override def setGui() = {
    val window = getWindow
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    val color = ContextCompat.getColor(this, R.color.toolbar_dark_transparent)
    getSupportActionBar.setBackgroundDrawable(new ColorDrawable(color))

    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    toolbar.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    toolbar.get.setTitleTextColor(theme.color1)
    toolbar.get.setImageButtonsColor(theme.color2)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      // Respond to the action bar's Up/Home button
      case android.R.id.home =>
        onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }
}
