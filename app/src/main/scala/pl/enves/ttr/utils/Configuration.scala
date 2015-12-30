package pl.enves.ttr.utils

import android.content.{SharedPreferences, Context}
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.R

object Configuration {
  private[this] val prefs = ContextRegistry.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
  private[this] val prefed: SharedPreferences.Editor = prefs.edit()
  def isPaid = ContextRegistry.context.getString(R.string.VERSION) == "PAID"

  def isFirstRun = prefs.getBoolean("FIRSTRUN", true)

  def isFirstRun_=(b: Boolean) = {
    prefed.putBoolean("FIRSTRUN", b)
    prefed.commit()
  }
}
