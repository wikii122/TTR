package pl.enves.ttr.utils.licenses

import android.content.Context
import android.graphics.Typeface
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.text.{SpannableStringBuilder, Spanned}
import pl.enves.androidx.{CustomTypefaceSpan, Logging}
import pl.enves.ttr.R

class LicenseFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  val items = Array(
    (context.getString(R.string.credits_spray), LicenseFragment("licenses/APL2.txt")),
    (context.getString(R.string.credits_font), LicenseFragment("licenses/OFL.txt"))
  )

  override def getPageTitle(position: Int): CharSequence = {
    val text = items(position)._1
    val typeface: Typeface = Typeface.createFromAsset(context.getAssets, "fonts/comfortaa.ttf")
    val ssb: SpannableStringBuilder = new SpannableStringBuilder(text)
    ssb.setSpan(new CustomTypefaceSpan(typeface), 0, text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return ssb
  }

  override def getCount: Int = items.length

  override def getItem(position: Int): Fragment = items(position)._2
}
