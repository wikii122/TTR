package pl.enves.androidx.helpers

import android.content.Intent
import pl.enves.androidx.context.ContextRegistry

trait IntentHelper {
  implicit class intentHelper(intent: Intent) {
    def start() = ContextRegistry.context.startActivity(intent)
  }
}
