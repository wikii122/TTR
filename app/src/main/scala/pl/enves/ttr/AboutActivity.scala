package pl.enves.ttr

import android.content.{ActivityNotFoundException, Intent}
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.{ImageButton, TextView}
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.ToolbarActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.utils.{Configuration, LogoUtils}

class AboutActivity extends ToolbarActivity with LogoUtils {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.about_layout)

    setupToolbar(R.id.about_toolbar)

    val envesImageButton = find[ImageButton](R.id.image_button_enves)

    alignLogo()
    displayVersion()

    envesImageButton onClick onEnves
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
    val descriptionText1 = find[TextView](R.id.text_game_description_1)
    val descriptionText2 = find[TextView](R.id.text_game_description_2)
    val descriptionText3 = find[TextView](R.id.text_game_description_3)
    val licenseText = find[TextView](R.id.text_game_license)

    createdByText.setTypeface(typeface)
    descriptionText1.setTypeface(typeface)
    descriptionText2.setTypeface(typeface)
    descriptionText3.setTypeface(typeface)
    licenseText.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    //get default theme
    val t = Theme.apply(getResources, Configuration.defaultColorThemeId)

    //colorize logo
    setLogoColorTheme(t)

    //texts
    val createdByText = find[TextView](R.id.text_created_by)
    val descriptionText1 = find[TextView](R.id.text_game_description_1)
    val descriptionText2 = find[TextView](R.id.text_game_description_2)
    val descriptionText3 = find[TextView](R.id.text_game_description_3)
    val licenseText = find[TextView](R.id.text_game_license)

    createdByText.setTextColor(t.color1)
    descriptionText1.setTextColor(t.color2)
    descriptionText2.setTextColor(t.color1)
    descriptionText3.setTextColor(t.color2)
    licenseText.setTextColor(t.color1)

    //and everything else
    super.setColorTheme(t)
  }
}