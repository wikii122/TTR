package pl.enves.ttr.utils

import com.google.android.gms.ads.{AdRequest, AdView}
import pl.enves.androidx.ExtendedActivity

trait AdUtils extends ExtendedActivity {

  def loadAd(adView: AdView): Unit = {
    val adRequest = createAdRequest()
    adView.loadAd(adRequest)
  }

  //TODO: add our phones ids as test devices
  //TODO: targeting
  //TODO: keywords
  private def createAdRequest(): AdRequest = {
    return new AdRequest.Builder()
      .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
      .build()
  }
}
