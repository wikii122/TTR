package pl.enves.ttr

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.text.{SpannableStringBuilder, Spanned}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import pl.enves.androidx._
import pl.enves.ttr.utils.styled.{StyledFragment, ToolbarActivity}
import pl.enves.ttr.utils.themes.Theme

class LicensesActivity extends ToolbarActivity {
  private[this] var viewPager: Option[ViewPager] = None
  private[this] var tabLayout: Option[TabLayout] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.licenses_layout)

    setupToolbar(R.id.licenses_toolbar)

    viewPager = Some(find[ViewPager](R.id.licenses_viewpager))
    tabLayout = Some(find[TabLayout](R.id.licenses_tabs))

    // Set viewPager's PagerAdapter so that it can display items
    viewPager.get.setAdapter(new LicenseFragmentPagerAdapter(getSupportFragmentManager, LicensesActivity.this))

    // Give the TabLayout the ViewPager
    tabLayout.get.setupWithViewPager(viewPager.get)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    tabLayout.get.setTabTextColors(theme.color1, theme.color2)
    tabLayout.get.setSelectedTabIndicatorColor(theme.color2)
  }
}

class LicenseFragment extends StyledFragment with Logging {
  private var textView: Option[TextView] = None

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_license, container, false)
    textView = Some(find[TextView](view, R.id.license_text))
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val textPath = getArguments.getString("TEXT_PATH")
    val is = getContext.getAssets.open(textPath)
    val textString = IOUtils.readText(is)
    textView.get.setText(textString)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    textView.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    textView.get.setTextColor(theme.color1)
  }
}

object LicenseFragment {
  def apply(path: String): LicenseFragment = {
    val licenseFragment = new LicenseFragment
    val args: Bundle = new Bundle()
    args.putString("TEXT_PATH", path)
    licenseFragment.setArguments(args)
    return licenseFragment
  }
}

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