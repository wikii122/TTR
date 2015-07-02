package pl.enves.ttr.utils.androidx

import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

trait ButtonHelpers {
  implicit class ButtonHelper(button: Button) {
    def onClick(function: View => Unit) = button setOnClickListener new OnClickListener {
      override def onClick(v: View): Unit = function(v)
    }

    def enable() = button.setEnabled(true)
    def disable() = button.setEnabled(false)
  }
}
