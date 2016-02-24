package pl.enves.ttr

import android.content.{ActivityNotFoundException, Intent}
import android.graphics.{Color, Typeface}
import android.net.Uri
import android.os.Bundle
import android.view.{View, WindowManager}
import android.widget.{TextView, ImageButton}
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.StyledActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.utils.{Configuration, LogoUtils}

class AboutActivity extends StyledActivity with LogoUtils {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.about_layout)

    val backButton = find[ImageButton](R.id.button_back_to_settings)
    val envesImageButton = find[ImageButton](R.id.image_button_enves)

    alignLogo()
    displayVersion()

    backButton.setColor(Color.BLACK)
    backButton onClick onBack
    envesImageButton onClick onEnves
  }

  override def onStart() = {
    super.onStart()

    setGui()
  }

  override def setGui(): Unit = {
    val window = getWindow
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
  }

  private def onBack(v: View): Unit = {
    onBackPressed()
  }

  private def onEnves(v: View): Unit = {
    val url = "http://enves.pl"
    val intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
      startActivity(intent)
    } catch {
      case e: ActivityNotFoundException =>
        error("no browser found, cannot open browser")
    }
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    setLogoTypeface(typeface)

    val createdByText = find[TextView](R.id.text_created_by)
    val descriptionText = find[TextView](R.id.text_game_description)
    val licenseText = find[TextView](R.id.text_game_license)

    createdByText.setTypeface(typeface)
    descriptionText.setTypeface(typeface)
    licenseText.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    // no call to super as we don't want background color to be changed

    //use default theme to colorize logo
    val t = Theme.apply(getResources, Configuration.defaultColorThemeId)
    setLogoColorTheme(t)
  }
}