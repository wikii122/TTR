package pl.enves.androidx.api

import android.content.{Context, Intent}

trait IntentHelper {
  implicit class intentHelper(intent: Intent) {
    def start()(implicit context: Context) = context.startActivity(intent)
  }
}
