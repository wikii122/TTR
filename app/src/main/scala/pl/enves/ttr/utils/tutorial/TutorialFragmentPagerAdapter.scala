package pl.enves.ttr.utils.tutorial

import android.content.Context
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import pl.enves.androidx.Logging
import pl.enves.ttr.R

class TutorialFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  val items = Array(
    AnimationFragment(R.string.tutorial_figures, R.xml.tutorial_figures_animation, autoPlay = true),
    AnimationFragment(R.string.tutorial_rotations, R.xml.tutorial_rotations_animation, autoPlay = false),
    AnimationFragment(R.string.tutorial_goals, R.xml.tutorial_goals_animation, autoPlay = false),
    TripleTextFragment(R.string.tutorial_standard, R.string.tutorial_network, R.string.tutorial_bot)
  )

  override def getCount: Int = items.length

  override def getItem(position: Int): Fragment = items(position)
}
