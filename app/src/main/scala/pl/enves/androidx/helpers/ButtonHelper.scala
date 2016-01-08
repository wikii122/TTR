package pl.enves.androidx.helpers

import android.content.res.ColorStateList
import android.graphics.{PorterDuff, PorterDuffColorFilter, Typeface}
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.View.OnClickListener
import android.widget._
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.ColorTypes.ColorAndroid
import pl.enves.androidx.color.filters.MaskDrawable

trait ButtonHelper {

  implicit class ButtonHelper(button: Button) {
    def onClick(function: View => Unit) = button setOnClickListener new OnClickListener {
      override def onClick(v: View): Unit = function(v)
    }

    def enable() = button.setEnabled(true)
    def disable() = button.setEnabled(false)
  }
}

trait TextButtonHelper {
  implicit class TextButtonHelper(button: TextView) {
    def onClick(function: View => Unit) = button setOnClickListener new OnClickListener {
      override def onClick(v: View): Unit = function(v)
    }

    def enable() = button.setEnabled(true)
    def disable() = button.setEnabled(false)
  }
}

trait ImageButtonHelper {
  implicit class ImageButtonHelper(button: ImageButton) {
    def onClick(function: View => Unit) = button setOnClickListener new OnClickListener {
      override def onClick(v: View): Unit = function(v)
    }

    def enable() = button.setEnabled(true)
    def disable() = button.setEnabled(false)

    def setColor(color: ColorAndroid): Unit = {
      button.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN))
    }

    def setColorMask(red: ColorAndroid, green: ColorAndroid, blue: ColorAndroid): Unit = {
      button.getDrawable match {
        case bitmapDrawable: BitmapDrawable =>
          bitmapDrawable.mask(red, green, blue)
        case _ =>
      }
    }
  }
}

trait DoubleButtonHelper {
  implicit class DoubleButtonHelper(buttons: (Button, Button)) {
    def onClick(function: View => Unit): Unit = {
      buttons._1 setOnClickListener new OnClickListener {
        override def onClick(v: View): Unit = function(v)
      }
      buttons._2 setOnClickListener new OnClickListener {
        override def onClick(v: View): Unit = function(v)
      }
    }

    def enable(): Unit = {
      buttons._1.setEnabled(true)
      buttons._2.setEnabled(true)
    }

    def disable(): Unit = {
      buttons._1.setEnabled(false)
      buttons._2.setEnabled(false)
    }

    def setTypeface(typeface: Typeface): Unit = {
      buttons._1.setTypeface(typeface)
      buttons._2.setTypeface(typeface)
    }

    def setTextColor(color1: ColorAndroid, color2: ColorAndroid): Unit = {
      buttons._1.setTextColor(color1)
      buttons._2.setTextColor(color2)
    }

    def setTextColor(color1: ColorStateList, color2: ColorStateList): Unit = {
      buttons._1.setTextColor(color1)
      buttons._2.setTextColor(color2)
    }

    def setVisibility(visibility: Int): Unit = {
      buttons._1.setVisibility(visibility)
      buttons._2.setVisibility(visibility)
    }
  }
}

trait SwitchHelper {
  implicit class SwitchHelper(switch: Switch) {
    def onCheck(function: (CompoundButton, Boolean) => Unit): Unit = {
      switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        override def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean): Unit = function(buttonView, isChecked)
      })
    }

    def enable(): Unit = switch.setEnabled(true)
    def disable(): Unit = switch.setEnabled(false)
  }
}