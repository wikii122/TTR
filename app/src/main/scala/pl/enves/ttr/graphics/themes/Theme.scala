package pl.enves.ttr.graphics.themes

import android.content.res.Resources
import org.json.{JSONException, JSONObject}
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics.ColorTypes.ColorAndroid

case class Theme(
                  background: ColorAndroid,
                  outer1: ColorAndroid,
                  outer2: ColorAndroid,
                  text: ColorAndroid,
                  winner: ColorAndroid
                  ) extends Logging {

  def toJsonObject: JSONObject = {
    val jSONObject: JSONObject = new JSONObject()
    try {
      jSONObject.put("background", background)
      jSONObject.put("outer1", outer1)
      jSONObject.put("outer2", outer2)
      jSONObject.put("text", text)
      jSONObject.put("winner", winner)
    } catch {
      case (e: JSONException) => error(e.toString)
    }
    return jSONObject
  }
}

object Theme extends Logging {
  def apply(): Theme = new Theme(0, 0, 0, 0, 0)

  def apply(resources: Resources, arrayName: Int): Theme = {
    if (arrayName == -1) return Theme()

    val themeArray: Array[Int] = resources.getIntArray(arrayName)
    return new Theme(
      themeArray(0),
      themeArray(1),
      themeArray(2),
      themeArray(3),
      themeArray(4)
    )
  }

  def apply(json: String): Theme = {
    try {
      val jSONObject = new JSONObject(json)
      return new Theme(
        jSONObject.getInt("background"),
        jSONObject.getInt("outer1"),
        jSONObject.getInt("outer2"),
        jSONObject.getInt("text"),
        jSONObject.getInt("winner")
      )
    } catch {
      case (e: JSONException) =>
        error(e.toString)
        return Theme()
    }
  }
}