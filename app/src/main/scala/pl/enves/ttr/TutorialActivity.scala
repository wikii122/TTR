package pl.enves.ttr

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view._
import android.widget.{Button, TextView}
import pl.enves.androidx.helpers._
import pl.enves.androidx.{ExtendedActivity, Logging}

abstract class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected val fontPath = "fonts/comfortaa.ttf"

  protected def changeFont(view: View, id: Int, typeface: Typeface): Unit = {
    val textView = find[TextView](view, id)
    textView.setTypeface(typeface)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(getLayout, container, false)
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeFont(view, getTitle, typeface)
    changeFont(view, getText, typeface)
    return view
  }

  def getLayout: Int

  def getTitle: Int

  def getText: Int
}

class FiguresFragment extends ExtendedFragment {
  override def getLayout = R.layout.fragment_tutorial_figures

  override def getTitle = R.id.tutorial_figures_title

  override def getText = R.id.tutorial_figures_text
}

class RotationsFragment extends ExtendedFragment {
  override def getLayout = R.layout.fragment_tutorial_rotations

  override def getTitle = R.id.tutorial_rotations_title

  override def getText = R.id.tutorial_rotations_text
}

class GoalsFragment extends ExtendedFragment {
  override def getLayout = R.layout.fragment_tutorial_goals

  override def getTitle = R.id.tutorial_goals_title

  override def getText = R.id.tutorial_goals_text
}

class StandardFragment extends ExtendedFragment {
  override def getLayout = R.layout.fragment_tutorial_standard

  override def getTitle = R.id.tutorial_standard_title

  override def getText = R.id.tutorial_standard_text
}

class NetworkFragment extends ExtendedFragment {
  override def getLayout = R.layout.fragment_tutorial_network

  override def getTitle = R.id.tutorial_network_title

  override def getText = R.id.tutorial_network_text
}

class TutorialFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  final val PAGE_COUNT = 5

  override def getCount: Int = {
    return PAGE_COUNT
  }

  override def getItem(position: Int): Fragment = {
    val args: Bundle = new Bundle()
    val fragment = position match {
      case 0 => new FiguresFragment()
      case 1 => new RotationsFragment
      case 2 => new GoalsFragment
      case 3 => new StandardFragment()
      case 4 => new NetworkFragment()
    }
    fragment.setArguments(args)
    return fragment
  }
}

class TutorialActivity extends ExtendedActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tutorial_layout)

    val viewPager: ViewPager = find[ViewPager](R.id.tutorial_viewpager)
    val adapter = new TutorialFragmentPagerAdapter(getSupportFragmentManager, TutorialActivity.this)
    viewPager.setAdapter(adapter)

    val skipButton = find[Button](R.id.tutorial_skip_button)
    skipButton onClick onSkipPressed

    val nextButton = find[Button](R.id.tutorial_next_button)
    nextButton onClick onNextPressed

    val doneButton = find[Button](R.id.tutorial_done_button)
    doneButton onClick onDonePressed

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}

      override def onPageSelected(position: Int): Unit = {
        if (position == adapter.getCount - 1) {
          skipButton.setVisibility(View.INVISIBLE)
          nextButton.setVisibility(View.GONE)
          doneButton.setVisibility(View.VISIBLE)
        } else {
          skipButton.setVisibility(View.VISIBLE)
          doneButton.setVisibility(View.GONE)
          nextButton.setVisibility(View.VISIBLE)
        }
      }

      override def onPageScrollStateChanged(state: Int): Unit = {}
    })
  }

  override def onStart() = {
    super.onStart()

    setBottomBarGui()

    applyCustomFont("fonts/comfortaa.ttf")
  }

  override def onPause() {
    super.onPause()
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val skipButton = find[Button](R.id.tutorial_skip_button)
    skipButton.setTypeface(typeface)

    val nextButton = find[Button](R.id.tutorial_next_button)
    nextButton.setTypeface(typeface)

    val doneButton = find[Button](R.id.tutorial_done_button)
    doneButton.setTypeface(typeface)
  }

  private[this] def onSkipPressed(v: View) = {
    onBackPressed()
  }

  private[this] def onNextPressed(v: View) = {
    val viewPager: ViewPager = find[ViewPager](R.id.tutorial_viewpager)
    viewPager.setCurrentItem(viewPager.getCurrentItem + 1)
  }

  private[this] def onDonePressed(v: View) = {
    onBackPressed()
  }
}
