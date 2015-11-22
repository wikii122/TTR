package pl.enves.ttr

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.text.{SpannableStringBuilder, Spanned}
import android.view.MenuItem
import android.widget.TextView
import pl.enves.androidx._

class LicensesActivity extends ExtendedActivity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.licenses_layout)

    val toolbar = find[Toolbar](R.id.licenses_toolbar)
    setSupportActionBar(toolbar)

    // Get the ViewPager and set it's PagerAdapter so that it can display items
    val viewPager: ViewPager = find[ViewPager](R.id.licenses_viewpager)
    viewPager.setAdapter(new LicenseFragmentPagerAdapter(getSupportFragmentManager, LicensesActivity.this))

    // Give the TabLayout the ViewPager
    val tabLayout: TabLayout = find[TabLayout](R.id.licenses_tabs)
    tabLayout.setupWithViewPager(viewPager)
  }

  override def onStart() = {
    super.onStart()

    setToolbarGui()

    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    applyCustomFont("fonts/comfortaa.ttf")

    val prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val theme = getSavedTheme(prefs)
    setColors(theme.background, theme.outer1, theme.outer2)
  }

  override def onPause() {
    super.onPause()
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val toolbar = find[Toolbar](R.id.licenses_toolbar)
    for (i <- 0 until toolbar.getChildCount) {
      toolbar.getChildAt(i) match {
        case view: TextView =>
          view.setTypeface(typeface)
        case _ =>
      }
    }
  }

  private[this] def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val toolbar = find[Toolbar](R.id.licenses_toolbar)
    toolbar.setTitleTextColor(content1)

    val tabLayout: TabLayout = find[TabLayout](R.id.licenses_tabs)
    tabLayout.setTabTextColors(content1, content2)
    tabLayout.setSelectedTabIndicatorColor(content2)

    toolbar.getRootView.setBackgroundColor(background)
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

class LicenseFragment extends ExtendedFragment with Logging {
  override protected val layout: Int = R.layout.fragment_license

  protected val text: Int = R.id.license_text

  override def onStart(): Unit = {
    super.onStart()

    val textPath = getArguments.getString("TEXT_PATH")
    val is = getContext.getAssets.open(textPath)
    val textString = IOUtils.readText(is)

    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeText(getView, text, textString)
    changeFont(getView, text, typeface)

    val prefs = getActivity.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val theme = getSavedTheme(prefs)
    setColors(theme.background, theme.outer1, theme.outer2)
  }

  private[this] def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val text = find[TextView](getView, R.id.license_text)
    text.setTextColor(content1)
  }
}

object LicenseFragment {
  def apply(path: String, number: Int): LicenseFragment = {
    val licenseFragment = new LicenseFragment
    val args: Bundle = new Bundle()
    args.putString("TEXT_PATH", path)
    args.putInt("NUMBER", number)
    licenseFragment.setArguments(args)
    return licenseFragment
  }
}

class LicenseFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  val items = Array(
    (context.getString(R.string.credits_spray), LicenseFragment("licenses/APL2.txt", 0)),
    (context.getString(R.string.credits_font), LicenseFragment("licenses/OFL.txt", 1))
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