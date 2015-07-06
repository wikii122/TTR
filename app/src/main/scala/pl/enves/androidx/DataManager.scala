package pl.enves.androidx

import android.content.Context

import scala.reflect.runtime.universe._

/**
 * Key-value string storage.
 */
class DataManager(name: String) {
  def get[A: TypeTag] (key: String)(implicit context: Context) = {
    val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    typeOf[A] match {
      case t if t =:= typeOf[String] => preferences.getString(key, "")
      case t if t =:= typeOf[Boolean] => preferences.getBoolean(key, false)
      case t if t =:= typeOf[Int] => preferences.getInt(key, 0)
      case t if t =:= typeOf[Float] => preferences.getFloat(key, 0.0f)
      case t if t =:= typeOf[Double] => preferences.getFloat(key, 0.0f).toDouble
      case _ => throw new IllegalArgumentException(s"Unsupported argument type: ${typeOf[A]}")
    }
  }

  def set(key: String, value: Any)(implicit context: Context) = {
    val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()

    value match {
      case s: String => editor.putString(key, s)
      case i: Int => editor.putInt(key, i)
      case b: Boolean => editor.putBoolean(key, b)
      case f: Float => editor.putFloat(key, f)
      case d: Double => editor.putFloat(key, d.toFloat)
      case a => throw new IllegalArgumentException(s"Unsupported argument type: ${a.getClass}")
    }

    editor.commit()
  }

  def apply(key:String) (implicit context: Context) = get(key)

  def update(key: String, value: Any) (implicit context: Context) = set(key, value)
}
