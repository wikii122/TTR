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

import scala.collection.mutable
import scala.util.Random

class AboutActivity extends ToolbarActivity with LogoUtils {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.about_layout)

    setupToolbar(R.id.about_toolbar)

    val envesImageButton = find[ImageButton](R.id.image_button_enves)
    val authorsText = find[TextView](R.id.text_app_authors)

    alignLogo()
    displayVersion()

    displayAuthors(authorsText)

    envesImageButton onClick onEnves

    authorsText onClick displayAuthors
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

  private def displayAuthors(v: View): Unit = {
    // alphabetically
    val authors = Seq(
      s"Łukasz Butryn",
      s"Patrycja Marciniak",
      s"Piotr Jankowski",
      s"Wiktor Ślęczka"
    )

    val buffer = mutable.ListBuffer(authors: _*)

    //randomly
    val randomly = new mutable.StringBuilder
    val generator = new Random()
    while (buffer.nonEmpty) {
      randomly ++= buffer.remove(generator.nextInt(buffer.size))
      if (buffer.nonEmpty) {
        randomly ++= ", "
      }
    }

    val authorsText = v.asInstanceOf[TextView]
    val authorsPrefix = getResources.getString(R.string.app_authors)

    authorsText.setText(authorsPrefix.format(randomly.result()))
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    setLogoTypeface(typeface)

    val createdByText = find[TextView](R.id.text_created_by)

    val descriptionText1 = find[TextView](R.id.text_app_description_1)
    val descriptionText2 = find[TextView](R.id.text_app_description_2)
    val descriptionText3 = find[TextView](R.id.text_app_description_3)

    val copyrightText = find[TextView](R.id.text_app_copyright)
    val authorsText = find[TextView](R.id.text_app_authors)

    val licenseText1 = find[TextView](R.id.text_app_license_1)
    val licenseText2 = find[TextView](R.id.text_app_license_2)
    val licenseText3 = find[TextView](R.id.text_app_license_3)
    val licenseText4 = find[TextView](R.id.text_app_license_4)

    createdByText.setTypeface(typeface)

    descriptionText1.setTypeface(typeface)
    descriptionText2.setTypeface(typeface)
    descriptionText3.setTypeface(typeface)

    copyrightText.setTypeface(typeface)
    authorsText.setTypeface(typeface)

    licenseText1.setTypeface(typeface)
    licenseText2.setTypeface(typeface)
    licenseText3.setTypeface(typeface)
    licenseText4.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    //get default theme
    val t = Theme.apply(getResources, Configuration.defaultColorThemeId)

    //colorize logo
    setLogoColorTheme(t)

    //texts
    val createdByText = find[TextView](R.id.text_created_by)

    val descriptionText1 = find[TextView](R.id.text_app_description_1)
    val descriptionText2 = find[TextView](R.id.text_app_description_2)
    val descriptionText3 = find[TextView](R.id.text_app_description_3)

    val copyrightText = find[TextView](R.id.text_app_copyright)
    val authorsText = find[TextView](R.id.text_app_authors)

    val licenseText1 = find[TextView](R.id.text_app_license_1)
    val licenseText2 = find[TextView](R.id.text_app_license_2)
    val licenseText3 = find[TextView](R.id.text_app_license_3)
    val licenseText4 = find[TextView](R.id.text_app_license_4)

    createdByText.setTextColor(t.color1)

    descriptionText1.setTextColor(t.color2)
    descriptionText2.setTextColor(t.color1)
    descriptionText3.setTextColor(t.color2)

    copyrightText.setTextColor(t.color1)
    authorsText.setTextColor(t.color2)

    licenseText1.setTextColor(t.color1)
    licenseText2.setTextColor(t.color2)
    licenseText3.setTextColor(t.color1)
    licenseText4.setTextColor(t.color2)

    //and everything else
    super.setColorTheme(t)
  }
}