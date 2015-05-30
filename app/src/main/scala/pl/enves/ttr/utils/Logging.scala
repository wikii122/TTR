package pl.enves.ttr.utils

import android.util.Log

/**
 * Mixin used to simplify logging
 */
trait Logging {
  protected val TAG = getClass.getName

  protected def log(msg: String) = Log.d(TAG, msg)

  protected def warn(msg: String) = Log.w(TAG, msg)

  protected def error(msg: String) = Log.e(TAG, msg)

  protected def wtf(msg: String) = Log.e(TAG, msg)
}
