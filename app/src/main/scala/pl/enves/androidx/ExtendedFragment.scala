package pl.enves.androidx

import android.content.{Context, SharedPreferences}
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.{LayoutInflater, View, ViewGroup}

abstract class ExtendedFragment extends Fragment {
  protected var prefs: Option[SharedPreferences] = None

  protected var number = 0

  protected def getLayoutId: Int

  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    prefs = Some(getActivity.getSharedPreferences("preferences", Context.MODE_PRIVATE))
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(getLayoutId, container, false)
    onOnCreateView(view)
    return view
  }

  override def onStart() {
    super.onStart()

    number = getArguments.getInt("NUMBER", 0)
  }

  protected def onOnCreateView(view: View): Unit = {}

  def onSelected(): Unit = {}

  def onDeSelected(): Unit = {}
}
