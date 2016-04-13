package pl.enves.androidx

import android.util.Log

/**
 * Mixin used to simplify logging
 */
trait Logging {
  private[this] val name = getClass.getName

  protected def log(msg: Any) = Log.d(name, msg.toString)

  protected def warn(msg: Any) = Log.w(name, msg.toString)

  protected def error(msg: Any) = Log.e(name, msg.toString)

  protected def wtf(msg: Any) = Log.wtf(name, msg.toString)
}
