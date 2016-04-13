package pl.enves.androidx

import android.content.Context
import android.widget.ArrayAdapter

trait ArrayAdapterEnumeration extends Enumeration {
  def getAdapter(context: Context): ArrayAdapter[String] = {
    val strings = values.toArray.map(v => v.toString)
    val aa = new ArrayAdapter[String](context, android.R.layout.simple_list_item_1, strings)
    return aa
  }
}
