package pl.enves.ttr

import android.content.{SharedPreferences, Context}
import android.content.res.TypedArray
import android.graphics.drawable.BitmapDrawable
import android.graphics.{BitmapFactory, Typeface}
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view._
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import pl.enves.androidx.ExtendedActivity
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.DrawableManip
import pl.enves.ttr.utils.themes.Theme

import scala.collection.mutable.ArrayBuffer

class ThemesActivity extends ExtendedActivity {
  private[this] var prefs: Option[SharedPreferences] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.themes_layout)

    val toolbar = find[Toolbar](R.id.themes_toolbar)
    setSupportActionBar(toolbar)

    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))

    val gridView = find[GridView](R.id.grid_themes)
    val themes = readDefaultThemes
    gridView.setAdapter(new ThemeImageAdapter(this, themes))

    gridView.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long): Unit = {
        val ed: SharedPreferences.Editor = prefs.get.edit()
        ed.putString("THEME", themes(position).toJsonObject.toString)
        ed.commit()

        onBackPressed()
      }
    })
  }

  override def onStart() = {
    super.onStart()

    setToolbarGui()

    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    applyCustomFont("fonts/comfortaa.ttf")

    val theme = getSavedTheme(prefs.get)
    setColors(theme.background, theme.outer1, theme.outer2)
  }

  override def onPause() {
    super.onPause()
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val toolbar = find[Toolbar](R.id.themes_toolbar)
    for (i <- 0 until toolbar.getChildCount) {
      toolbar.getChildAt(i) match {
        case view: TextView =>
          view.setTypeface(typeface)
        case _ =>
      }
    }
  }

  private[this] def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val toolbar = find[Toolbar](R.id.themes_toolbar)
    toolbar.setTitleTextColor(content1)

    toolbar.getRootView.setBackgroundColor(background)
  }

  private def readDefaultThemes: ArrayBuffer[Theme] = {
    val themes = new ArrayBuffer[Theme]()
    val resources = getResources
    val themeArrays: TypedArray = resources.obtainTypedArray(R.array.themes)
    for (i <- 0 until themeArrays.length) {
      themes.append(Theme(resources, themeArrays.getResourceId(i, -1)))
    }
    themeArrays.recycle()
    return themes
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

class ThemeImageAdapter(context: Context, themes: ArrayBuffer[Theme]) extends BaseAdapter with DrawableManip {

  override def getCount: Int = themes.size

  // create a new ImageView for each item referenced by the Adapter
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var imageView: ImageView = new ImageView(context)
    if (convertView == null) {
      // if it's not recycled, initialize some attributes
      //imageView.setLayoutParams(new AbsListView.LayoutParams(100, 100))
      //imageView.setPadding(0, 0, 0, 0)
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
      imageView.setAdjustViewBounds(true)
    } else {
      imageView = convertView.asInstanceOf[ImageView]
    }

    val res = context.getResources
    val drawable = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.board_mask))
    drawable.setAntiAlias(true)

    val theme = themes(position)
    maskColors(theme.background, theme.outer1, theme.outer2, drawable)

    imageView.setImageDrawable(drawable)
    return imageView
  }

  override def getItemId(position: Int): Long = 0

  override def getItem(position: Int): AnyRef = null
}