package pl.enves.ttr

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view._
import android.widget.Button
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.BottomBarActivity
import pl.enves.ttr.utils.themes.Theme
import pl.enves.ttr.utils.tutorial.{Selectable, TutorialFragmentPagerAdapter}

class TutorialActivity extends BottomBarActivity {

  private[this] var currentFragment = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tutorial_layout)

    val viewPager = find[ViewPager](R.id.tutorial_viewpager)
    val adapter = new TutorialFragmentPagerAdapter(getSupportFragmentManager, TutorialActivity.this)

    val skipButton = find[Button](R.id.tutorial_skip_button)
    val nextButton = find[Button](R.id.tutorial_next_button)
    val doneButton = find[Button](R.id.tutorial_done_button)

    skipButton onClick onSkipPressed
    nextButton onClick onNextPressed
    doneButton onClick onDonePressed

    viewPager.setAdapter(adapter)

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
        viewPager.getAdapter.instantiateItem(viewPager, currentFragment).asInstanceOf[Selectable].onDeSelected()
        currentFragment = position
        viewPager.getAdapter.instantiateItem(viewPager, currentFragment).asInstanceOf[Selectable].onSelected()
      }

      override def onPageScrollStateChanged(state: Int): Unit = {}
    })
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    val skipButton = find[Button](R.id.tutorial_skip_button)
    val nextButton = find[Button](R.id.tutorial_next_button)
    val doneButton = find[Button](R.id.tutorial_done_button)

    skipButton.setTypeface(typeface)
    nextButton.setTypeface(typeface)
    doneButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    //we don't want to change default background color here, so no call to super
  }

  private[this] def onSkipPressed(v: View) = {
    onBackPressed()
  }

  private[this] def onNextPressed(v: View) = {
    val viewPager = find[ViewPager](R.id.tutorial_viewpager)

    viewPager.setCurrentItem(viewPager.getCurrentItem + 1)
  }

  private[this] def onDonePressed(v: View) = {
    onBackPressed()
  }
}
