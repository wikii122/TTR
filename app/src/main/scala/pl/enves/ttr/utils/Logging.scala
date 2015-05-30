package pl.enves.ttr.utils

import android.util.Log

/**
 * Mixin used to simplify logging
 */
trait Logging {
  private[this] val TAG = getClass.getName

  private def log(msg: String) = Log.d(TAG, msg)

  private def warn(msg: String) = Log.w(TAG, msg)

  private def error(msg: String) = Log.e(TAG, msg)

  private def wtf(msg: String) = Log.e(TAG, msg)
}
