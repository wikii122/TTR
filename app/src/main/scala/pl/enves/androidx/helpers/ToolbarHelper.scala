package pl.enves.androidx.helpers

import android.graphics.Typeface
import android.support.v7.widget.Toolbar
import android.widget.{ImageButton, TextView}
import pl.enves.androidx.color.ColorTypes.ColorAndroid

trait ToolbarHelper {

  implicit class ToolbarHelper(toolbar: Toolbar) {
    def setTypeface(typeface: Typeface): Unit = {
      for (i <- 0 until toolbar.getChildCount) {
        toolbar.getChildAt(i) match {
          case view: TextView =>
            view.setTypeface(typeface)
          case _ =>
        }
      }
    }

    def setImageButtonsColor(color: ColorAndroid): Unit = {
      for (i <- 0 until toolbar.getChildCount) {
        toolbar.getChildAt(i) match {
          case view: ImageButton =>
            view.setColor(color)
          case _ =>
        }
      }
    }
  }

}