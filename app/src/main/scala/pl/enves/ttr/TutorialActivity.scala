package pl.enves.ttr

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.text.{SpannableStringBuilder, Spanned}
import android.view._
import android.widget.TextView
import pl.enves.androidx.{CustomTypefaceSpan, ExtendedActivity, Logging}
import pl.enves.ttr.utils.themes.Theme

class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected def changeFont(view: View, path: String): Unit = {
  }
}

class RulesFragment extends ExtendedFragment {
  private[this] def style(view: View, path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, path)

    val text1 = find[TextView](view, R.id.tutorial_rules_text_1)
    text1.setTypeface(typeface)

    val text2 = find[TextView](view, R.id.tutorial_rules_text_2)
    text2.setTypeface(typeface)

    val text3 = find[TextView](view, R.id.tutorial_rules_text_3)
    text3.setTypeface(typeface)

    val text4 = find[TextView](view, R.id.tutorial_rules_text_4)
    text4.setTypeface(typeface)

    val text5 = find[TextView](view, R.id.tutorial_rules_text_5)
    text5.setTypeface(typeface)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_tutorial_rules, container, false)
    style(view, "fonts/comfortaa.ttf")
    return view
  }
}

class StandardFragment extends ExtendedFragment {
  private[this] def style(view: View, path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, path)

    val text1 = find[TextView](view, R.id.tutorial_standard_text_1)
    text1.setTypeface(typeface)

    val text2 = find[TextView](view, R.id.tutorial_standard_text_2)
    text2.setTypeface(typeface)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_tutorial_standard, container, false)
    style(view, "fonts/comfortaa.ttf")
    return view
  }
}

class NetworkFragment extends ExtendedFragment {
  private[this] def style(view: View, path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, path)

    val text1 = find[TextView](view, R.id.tutorial_network_text_1)
    text1.setTypeface(typeface)

    val text2 = find[TextView](view, R.id.tutorial_network_text_2)
    text2.setTypeface(typeface)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_tutorial_network, container, false)
    style(view, "fonts/comfortaa.ttf")
    return view
  }
}


class TutorialFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  final val PAGE_COUNT = 3

  override def getCount: Int = {
    return PAGE_COUNT
  }

  override def getItem(position: Int): Fragment = {
    val args: Bundle = new Bundle()
    val fragment = position match {
      case 0 => new RulesFragment()
      case 1 => new StandardFragment()
      case 2 => new NetworkFragment()
    }
    fragment.setArguments(args)
    return fragment
  }

  override def getPageTitle(position: Int): CharSequence = {
    val text = position match {
      case 0 => context.getString(R.string.tutorial_rules_title)
      case 1 => context.getString(R.string.tutorial_standard_title)
      case 2 => context.getString(R.string.tutorial_network_title)
    }
    val typeface: Typeface = Typeface.createFromAsset(context.getAssets, "fonts/comfortaa.ttf")
    val ssb: SpannableStringBuilder = new SpannableStringBuilder(text)
    ssb.setSpan(new CustomTypefaceSpan(typeface), 0, text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return ssb
  }
}

class TutorialActivity extends ExtendedActivity {
  private[this] var displayUp = true

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tutorial_layout)

    val toolbar = find[Toolbar](R.id.tutorial_toolbar)
    setSupportActionBar(toolbar)

    // Get the ViewPager and set it's PagerAdapter so that it can display items
    val viewPager: ViewPager = find[ViewPager](R.id.tutorial_viewpager)
    viewPager.setAdapter(new TutorialFragmentPagerAdapter(getSupportFragmentManager, TutorialActivity.this))

    // Give the TabLayout the ViewPager
    val tabLayout: TabLayout = find[TabLayout](R.id.tutorial_tabs)
    tabLayout.setupWithViewPager(viewPager)

    val b: Bundle = getIntent.getExtras
    if (b != null) {
      displayUp = !b.getBoolean("FIRSTRUN", true)
    }
  }

  override def onStart() = {
    super.onStart()

    setToolbarGui()

    getSupportActionBar.setDisplayHomeAsUpEnabled(displayUp)

    applyCustomFont("fonts/comfortaa.ttf")

    val theme = Theme(getResources, R.array.theme_five)
    setColors(theme.background, theme.outer1, theme.outer2)
  }

  override def onPause() {
    super.onPause()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_tutorial, menu)
    return true
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val toolbar = find[Toolbar](R.id.tutorial_toolbar)
    for (i <- 0 until toolbar.getChildCount) {
      toolbar.getChildAt(i) match {
        case view: TextView =>
          view.setTypeface(typeface)
        case _ =>
      }
    }
  }

  private[this] def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val toolbar = find[Toolbar](R.id.tutorial_toolbar)
    toolbar.setTitleTextColor(content1)
    //toolbar.setBackgroundColor(color)

    toolbar.getRootView.setBackgroundColor(background)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      // Respond to the action bar's Up/Home button
      case android.R.id.home =>
        onBackPressed()
      case R.id.action_ok =>
        onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }
}
