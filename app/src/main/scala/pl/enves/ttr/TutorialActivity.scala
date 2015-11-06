package pl.enves.ttr

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view._
import android.widget.{ImageView, Button, TextView}
import pl.enves.androidx.helpers._
import pl.enves.androidx.{ExtendedActivity, Logging}

abstract class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected val fontPath = "fonts/comfortaa.ttf"

  protected def changeFont(view: View, id: Int, typeface: Typeface): Unit = {
    val textView = find[TextView](view, id)
    textView.setTypeface(typeface)
  }

  protected def changeText(view: View, id: Int, textId: Int): Unit = {
    val textView = find[TextView](view, id)
    textView.setText(textId)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(layout, container, false)
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeText(view, title, getTitleRes)
    changeText(view, text, getTextRes)
    changeFont(view, title, typeface)
    changeFont(view, text, typeface)
    val imageView = find[ImageView](view, image)
    imageView.setImageResource(getImageRes)

    return view
  }

  private val layout: Int = R.layout.fragment_tutorial

  private val title: Int = R.id.tutorial_title

  private val text: Int = R.id.tutorial_text

  private val image: Int = R.id.tutorial_image

  def getTitleRes: Int = 0

  def getTextRes: Int = 0

  def getImageRes: Int = R.drawable.tutorial_placeholder
}

class FiguresFragment extends ExtendedFragment {
  override def getTitleRes = R.string.tutorial_figures_title

  override def getTextRes = R.string.tutorial_figures
}

class RotationsFragment extends ExtendedFragment {
  override def getTitleRes = R.string.tutorial_rotations_title

  override def getTextRes = R.string.tutorial_rotations
}

class GoalsFragment extends ExtendedFragment {
  override def getTitleRes = R.string.tutorial_goals_title

  override def getTextRes = R.string.tutorial_goals
}

class StandardFragment extends ExtendedFragment {
  override def getTitleRes = R.string.tutorial_standard_title

  override def getTextRes = R.string.tutorial_standard
}

class NetworkFragment extends ExtendedFragment {
  override def getTitleRes = R.string.tutorial_network_title

  override def getTextRes = R.string.tutorial_network
}

class TutorialFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  val items = Array(
    new FiguresFragment(),
    new RotationsFragment(),
    new GoalsFragment(),
    new StandardFragment(),
    new NetworkFragment()
  )

  override def getCount: Int = items.length

  override def getItem(position: Int): Fragment = {
    val args: Bundle = new Bundle()
    val fragment = items(position)
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
