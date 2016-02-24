package pl.enves.ttr

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import pl.enves.ttr.utils.licenses.LicenseFragmentPagerAdapter
import pl.enves.ttr.utils.styled.ToolbarActivity
import pl.enves.ttr.utils.themes.Theme

class LicensesActivity extends ToolbarActivity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.licenses_layout)

    setupToolbar(R.id.licenses_toolbar)

    val viewPager = find[ViewPager](R.id.licenses_viewpager)
    val tabLayout = find[TabLayout](R.id.licenses_tabs)

    // Set viewPager's PagerAdapter so that it can display items
    viewPager.setAdapter(new LicenseFragmentPagerAdapter(getSupportFragmentManager, LicensesActivity.this))

    // Give the TabLayout the ViewPager
    tabLayout.setupWithViewPager(viewPager)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    val tabLayout = find[TabLayout](R.id.licenses_tabs)

    tabLayout.setTabTextColors(theme.color1, theme.color2)
    tabLayout.setSelectedTabIndicatorColor(theme.color2)
  }
}
