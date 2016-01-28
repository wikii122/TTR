package pl.enves.ttr.utils.licenses

import android.graphics.Typeface
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import pl.enves.androidx.{IOUtils, Logging}
import pl.enves.ttr.R
import pl.enves.ttr.utils.styled.StyledFragment
import pl.enves.ttr.utils.themes.Theme

class LicenseFragment extends StyledFragment with Logging {
  private var textView: Option[TextView] = None

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_license, container, false)
    textView = Some(find[TextView](view, R.id.license_text))
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

    val textPath = getArguments.getString("TEXT_PATH")
    val is = getContext.getAssets.open(textPath)
    val textString = IOUtils.readText(is)
    textView.get.setText(textString)
  }

  override def setTypeface(typeface: Typeface): Unit = {
    textView.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    textView.get.setTextColor(theme.color1)
  }
}

object LicenseFragment {
  def apply(path: String): LicenseFragment = {
    val licenseFragment = new LicenseFragment
    val args: Bundle = new Bundle()
    args.putString("TEXT_PATH", path)
    licenseFragment.setArguments(args)
    return licenseFragment
  }
}