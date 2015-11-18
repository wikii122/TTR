package pl.enves.androidx
package context

import android.content.Context
import android.os.{PersistableBundle, Bundle}
import android.support.v7.app.AppCompatActivity

object ContextRegistry {
  def context = ctx.get
  private var ctx: Option[Context] = None
  private def context_=(c: Context) = ctx = Some(c)
}

/**
 * Trait used to autoregister context of activity.
 * Note: super call should be made first in onCreate, onStart and onDestroy.
 */
trait ContextRegistry extends AppCompatActivity with Logging {
  private def register() = {
    ContextRegistry.context = this
    log("Context registered")
  }
  abstract override def onCreate(bundle: Bundle) = {
    register()
    super.onCreate(bundle)
  }

  abstract override def onStart() = {
    register()
    super.onStart()
  }

  abstract override def onResume() = {
    register()
    super.onResume()
  }
}
