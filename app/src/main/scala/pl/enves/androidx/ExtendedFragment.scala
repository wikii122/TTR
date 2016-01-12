package pl.enves.androidx

import android.support.v4.app.Fragment
import android.view.View

abstract class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]
}
