package pl.enves.ttr

import android.content.res.TypedArray
import android.content.{Context, SharedPreferences}
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view._
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.DrawableManip
import pl.enves.ttr.utils.styled.ToolbarActivity
import pl.enves.ttr.utils.themes.Theme

import scala.collection.mutable.ArrayBuffer

class ThemesActivity extends ToolbarActivity {
  private[this] var gridView: Option[GridView] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.themes_layout)

    setupToolbar(R.id.themes_toolbar)

    gridView = Some(find[GridView](R.id.grid_themes))

    val themes = readDefaultThemes
    gridView.get.setAdapter(new ThemeImageAdapter(this, themes))

    gridView.get.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long): Unit = {
        val ed: SharedPreferences.Editor = prefs.get.edit()
        ed.putString("THEME", themes(position).toJsonObject.toString)
        ed.commit()

        onBackPressed()
      }
    })
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
}

class ThemeImageAdapter(context: Context, themes: ArrayBuffer[Theme]) extends BaseAdapter with DrawableManip {

  override def getCount: Int = themes.size

  // create a new ImageView for each item referenced by the Adapter
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var imageView: ImageView = new ImageView(context)
    if (convertView == null) {
      // if it's not recycled, initialize some attributes
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
      imageView.setAdjustViewBounds(true)
    } else {
      imageView = convertView.asInstanceOf[ImageView]
    }

    val res = context.getResources
    val drawable = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.board_mask))
    drawable.setAntiAlias(true)

    val theme = themes(position)
    maskColors(theme.background, theme.color1, theme.color2, drawable)

    imageView.setImageDrawable(drawable)
    return imageView
  }

  override def getItemId(position: Int): Long = 0

  override def getItem(position: Int): AnyRef = null
}