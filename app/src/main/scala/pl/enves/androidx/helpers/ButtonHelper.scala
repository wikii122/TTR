package pl.enves.androidx.helpers

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, ImageButton, TextView}
import pl.enves.androidx.color.ColorTypes.ColorAndroid

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
  }
}