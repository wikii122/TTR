package pl.enves.androidx.helpers

import android.view.View
import android.view.View.OnClickListener
import android.widget.{TextView, Button}

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