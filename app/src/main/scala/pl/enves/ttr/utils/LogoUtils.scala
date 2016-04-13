package pl.enves.ttr.utils

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import pl.enves.androidx.views.ExtendedActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.{BuildConfig, R}

trait LogoUtils extends ExtendedActivity {
  private def alignUpsideDownText(text: TextView): Unit = {
    val fm = text.getPaint.getFontMetrics
    val descent = Math.round(fm.descent)
    text.setPadding(0, 0, 0, descent)
  }

  def alignLogo(): Unit = {
    val turnText = find[TextView](R.id.text_turn)

    alignUpsideDownText(turnText)
  }

  def displayVersion(): Unit = {
    val versionText = find[TextView](R.id.text_version)
    val versionCode: Int = BuildConfig.VERSION_CODE
    val versionName: String = BuildConfig.VERSION_NAME

    val version = s"v. $versionName build $versionCode"
    versionText.setText(version)
    versionText.setVisibility(View.VISIBLE)
  }

  def setLogoTypeface(typeface: Typeface): Unit = {
    val versionText = find[TextView](R.id.text_version)
    val ticTacText = find[TextView](R.id.text_tic_tac)
    val turnText = find[TextView](R.id.text_turn)
    val ttText = find[TextView](R.id.text_tt)
    val tText = find[TextView](R.id.text_t)

    versionText.setTypeface(typeface)
    ticTacText.setTypeface(typeface)
    turnText.setTypeface(typeface)
    ttText.setTypeface(typeface)
    tText.setTypeface(typeface)
  }

  def setLogoColorTheme(theme: Theme): Unit = {
    val versionText = find[TextView](R.id.text_version)
    val ticTacText = find[TextView](R.id.text_tic_tac)
    val turnText = find[TextView](R.id.text_turn)
    val ttText = find[TextView](R.id.text_tt)
    val tText = find[TextView](R.id.text_t)

    versionText.setTextColor(theme.color2)
    ticTacText.setTextColor(theme.color1)
    turnText.setTextColor(theme.color2)
    ttText.setTextColor(theme.color1)
    tText.setTextColor(theme.color2)
  }
}
