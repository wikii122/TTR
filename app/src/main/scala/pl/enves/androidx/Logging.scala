package pl.enves.androidx

import android.util.Log

/**
 * Mixin used to simplify logging
 */
trait Logging {
  private[this] val name = getClass.getName

  protected def log(msg: String) = Log.d(name, msg)

  protected def warn(msg: String) = Log.w(name, msg)

  protected def error(msg: String) = Log.e(name, msg)

  protected def wtf(msg: String) = Log.wtf(name, msg)
}
