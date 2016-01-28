package pl.enves.ttr.utils.tutorial

import android.graphics.Typeface
import android.os.Bundle
import android.view._
import android.widget.TextView
import pl.enves.androidx._
import pl.enves.ttr.R
import pl.enves.ttr.utils.styled.StyledFragment

class DoubleTextFragment extends StyledFragment with Selectable with Logging {
  private var textView1: Option[TextView] = None
  private var textView2: Option[TextView] = None

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_tutorial_double_text, container, false)
    textView1 = Some(find[TextView](view, R.id.tutorial_text_1))
    textView2 = Some(find[TextView](view, R.id.tutorial_text_2))
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val text1Res = getArguments.getInt("TEXT_1_RES", 0)
    val text2Res = getArguments.getInt("TEXT_2_RES", 0)

    textView1.get.setText(text1Res)
    textView2.get.setText(text2Res)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    textView1.get.setTypeface(typeface)
    textView2.get.setTypeface(typeface)
  }
}

object DoubleTextFragment {
  def apply(text1Res: Int, text2Res: Int): DoubleTextFragment = {
    val doubleTextFragment = new DoubleTextFragment
    val args: Bundle = new Bundle()
    args.putInt("TEXT_1_RES", text1Res)
    args.putInt("TEXT_2_RES", text2Res)
    doubleTextFragment.setArguments(args)
    return doubleTextFragment
  }
}