package pl.enves.ttr

import java.io.{IOException, InputStream}
import java.util
import java.util.concurrent.LinkedBlockingQueue

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.{Bitmap, BitmapFactory, Typeface}
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view._
import android.widget.{Button, ImageView, TextView}
import org.xmlpull.v1.{XmlPullParser, XmlPullParserException}
import pl.enves.androidx._
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.{BottomBarActivity, StyledFragment}
import pl.enves.ttr.utils.themes.Theme

class DoubleTextFragment extends StyledFragment with Logging {
  private var textView1: Option[TextView] = None
  private var textView2: Option[TextView] = None

  override protected def getLayoutId: Int = R.layout.fragment_tutorial_double_text

  override def onOnCreateView(view: View): Unit = {
    textView1 = Some(find[TextView](view, R.id.tutorial_text_1))
    textView2 = Some(find[TextView](view, R.id.tutorial_text_2))
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
  def apply(text1Res: Int, text2Res: Int, number: Int): DoubleTextFragment = {
    val doubleTextFragment = new DoubleTextFragment
    val args: Bundle = new Bundle()
    args.putInt("TEXT_1_RES", text1Res)
    args.putInt("TEXT_2_RES", text2Res)
    args.putInt("NUMBER", number)
    doubleTextFragment.setArguments(args)
    return doubleTextFragment
  }
}

class AnimationFragment extends StyledFragment with Logging {
  private var textView: Option[TextView] = None
  private var imageView: Option[ImageView] = None

  private val frameSpecs: util.ArrayList[FrameSpec] = new util.ArrayList[FrameSpec]()

  private val framesLoaded = new LinkedBlockingQueue[FrameData](2)

  private var frameToLoad = 0

  private var producerThread: Option[Thread] = None
  private var consumerThread: Option[Thread] = None

  private var lastFrameData: Option[FrameData] = None

  private var animate = false

  private var autoPlay = false

  private var wasAutoPlayed = false

  override protected def getLayoutId: Int = R.layout.fragment_tutorial_image_text

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    log("onCreate" + number)
    if (savedInstanceState != null) {
      autoPlay = savedInstanceState.getBoolean("AUTO_PLAY", false)
      wasAutoPlayed = false
    }
  }

  override def onOnCreateView(view: View): Unit = {
    textView = Some(find[TextView](view, R.id.tutorial_text))
    imageView = Some(find[ImageView](view, R.id.tutorial_image))
  }

  override def onStart(): Unit = {
    super.onStart()
    log("onStart" + number)

    val textRes = getArguments.getInt("TEXT_RES", 0)
    val animationRes = getArguments.getInt("ANIMATION_RES", 0)

    textView.get.setText(textRes)

    loadAnimation(animationRes, getContext)
    displayFirstFrame(imageView.get, getContext)
    autoPlay = autoPlay || getArguments.getBoolean("AUTO_PLAY", false)
    if (autoPlay && !wasAutoPlayed) {
      wasAutoPlayed = true
      startAnimation(imageView.get, getContext)
    }
  }

  override def setTypeface(typeface: Typeface): Unit = {
    textView.get.setTypeface(typeface)
  }

  override def onSelected(): Unit = {
    log("onSelected" + number)
    startAnimation(imageView.get, getContext)
  }

  override def onDeSelected(): Unit = {
    log("onDeSelected" + number)
    stopAnimation()
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    log("onSaveInstanceState" + number)
    outState.putBoolean("AUTO_PLAY", animate)
  }

  override def onStop(): Unit = {
    super.onStop()
    log("onStop" + number)
    autoPlay = animate
    stopAnimation()
    if (lastFrameData.isDefined) {
      lastFrameData.get.recycle()
    }
    lastFrameData = None
    frameToLoad = 0
    producerThread = None
    consumerThread = None
    wasAutoPlayed = false
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    log("onDestroy" + number)
    frameSpecs.clear()
  }

  private def setImage(imageView: ImageView, frameData: FrameData) {
    getActivity.runOnUiThread(new Runnable() {
      override def run(): Unit = {
        imageView.setImageBitmap(frameData.getBitmap)
        if (lastFrameData.isDefined) {
          lastFrameData.get.recycle()
        }
        lastFrameData = Some(frameData)
      }
    })
  }

  class FrameData(bitmap: Bitmap, duration: Int) {
    def getBitmap: Bitmap = bitmap

    def getDuration: Int = duration

    def recycle(): Unit = bitmap.recycle()
  }

  class FrameSpec(rawData: Array[Byte], duration: Int) {
    def getDuration = duration

    def makeData(context: Context): FrameData = {
      return new FrameData(BitmapFactory.decodeByteArray(rawData, 0, rawData.length), duration)
    }
  }


  def loadAnimation(resourceId: Int, context: Context): Unit = {
    val parser: XmlResourceParser = context.getResources.getXml(resourceId)
    frameSpecs.clear()
    try {
      var eventType = parser.getEventType
      while (eventType != XmlPullParser.END_DOCUMENT) {
        eventType match {
          case XmlPullParser.START_DOCUMENT =>
          case XmlPullParser.START_TAG =>

            if (parser.getName.equals("item")) {
              var imagePath = ""
              var duration = 0

              for (i <- 0 until parser.getAttributeCount) {
                if (parser.getAttributeName(i).equals("drawable")) {
                  imagePath = parser.getAttributeValue(i).substring(0)
                }
                else if (parser.getAttributeName(i).equals("duration")) {
                  duration = parser.getAttributeIntValue(i, 1000)
                }
              }

              try {
                val stream: InputStream = context.getAssets.open(imagePath)
                val rawData = IOUtils.readBytes(stream)
                stream.close()
                val frameSpec = new FrameSpec(rawData, duration)
                frameSpecs.add(frameSpec)
              } catch {
                case e: IOException =>
                  error(e.getMessage)
              }
            }

          case XmlPullParser.END_TAG =>
          case XmlPullParser.TEXT =>
        }
        eventType = parser.next()
      }
    }
    catch {
      case e: XmlPullParserException =>
        e.printStackTrace()
    }
  }

  class Producer(context: Context) extends Runnable {
    override def run(): Unit = {
      while (true) {
        val frameData = frameSpecs.get(frameToLoad).makeData(context)
        try {
          framesLoaded.put(frameData)
        } catch {
          case e: InterruptedException =>
            frameData.recycle()
            return
        }
        frameToLoad += 1
        if (frameToLoad == frameSpecs.size()) {
          frameToLoad = 0
        }
      }
    }
  }

  class Consumer(imageView: ImageView) extends Runnable {
    override def run(): Unit = {
      while (true) {
        var frameData: Option[FrameData] = None
        try {
          frameData = Some(framesLoaded.take())
        } catch {
          case e: InterruptedException =>
            return
        }
        setImage(imageView, frameData.get)
        try {
          Thread.sleep(frameData.get.getDuration)
        } catch {
          case e: InterruptedException =>
            return
        }
      }
    }
  }

  def displayFirstFrame(imageView: ImageView, context: Context): Unit = {
    val frameData = frameSpecs.get(0).makeData(context)
    setImage(imageView, frameData)
  }

  def startAnimation(imageView: ImageView, context: Context): Unit = {
    if (!animate) {
      animate = true
      consumerThread = Some(new Thread(new Consumer(imageView)))
      consumerThread.get.start()
      producerThread = Some(new Thread(new Producer(context)))
      producerThread.get.start()
    }
  }

  def stopAnimation(): Unit = {
    if (animate) {
      animate = false
      producerThread.get.interrupt()
      producerThread.get.join()
      producerThread = None
      consumerThread.get.interrupt()
      consumerThread.get.join()
      consumerThread = None

      while (framesLoaded.size() != 0) {
        framesLoaded.take().recycle()
      }
      frameToLoad = 0
    }
  }
}

object AnimationFragment {
  def apply(textRes: Int, animationRes: Int, autoPlay: Boolean, number: Int): AnimationFragment = {
    val animationFragment = new AnimationFragment
    val args: Bundle = new Bundle()
    args.putInt("TEXT_RES", textRes)
    args.putInt("ANIMATION_RES", animationRes)
    args.putBoolean("AUTO_PLAY", autoPlay)
    args.putInt("NUMBER", number)
    animationFragment.setArguments(args)
    return animationFragment
  }
}

class TutorialFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  val items = Array(
    AnimationFragment(R.string.tutorial_figures, R.xml.tutorial_figures_animation, autoPlay = true, 1),
    AnimationFragment(R.string.tutorial_rotations, R.xml.tutorial_rotations_animation, autoPlay = false, 2),
    AnimationFragment(R.string.tutorial_goals, R.xml.tutorial_goals_animation, autoPlay = false, 3),
    DoubleTextFragment(R.string.tutorial_standard, R.string.tutorial_network, 4)
  )

  override def getCount: Int = items.length

  override def getItem(position: Int): Fragment = items(position)
}

class TutorialActivity extends BottomBarActivity {
  private[this] var adapter: Option[TutorialFragmentPagerAdapter] = None
  private[this] var viewPager: Option[ViewPager] = None

  private[this] var skipButton: Option[Button] = None
  private[this] var nextButton: Option[Button] = None
  private[this] var doneButton: Option[Button] = None

  private[this] var currentFragment = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tutorial_layout)

    viewPager = Some(find[ViewPager](R.id.tutorial_viewpager))
    adapter = Some(new TutorialFragmentPagerAdapter(getSupportFragmentManager, TutorialActivity.this))

    skipButton = Some(find[Button](R.id.tutorial_skip_button))
    nextButton = Some(find[Button](R.id.tutorial_next_button))
    doneButton = Some(find[Button](R.id.tutorial_done_button))

    skipButton.get onClick onSkipPressed
    nextButton.get onClick onNextPressed
    doneButton.get onClick onDonePressed

    viewPager.get.setAdapter(adapter.get)

    viewPager.get.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}

      override def onPageSelected(position: Int): Unit = {
        if (position == adapter.get.getCount - 1) {
          skipButton.get.setVisibility(View.INVISIBLE)
          nextButton.get.setVisibility(View.GONE)
          doneButton.get.setVisibility(View.VISIBLE)
        } else {
          skipButton.get.setVisibility(View.VISIBLE)
          doneButton.get.setVisibility(View.GONE)
          nextButton.get.setVisibility(View.VISIBLE)
        }
        viewPager.get.getAdapter.instantiateItem(viewPager.get, currentFragment).asInstanceOf[ExtendedFragment].onDeSelected()
        currentFragment = position
        viewPager.get.getAdapter.instantiateItem(viewPager.get, currentFragment).asInstanceOf[ExtendedFragment].onSelected()
      }

      override def onPageScrollStateChanged(state: Int): Unit = {}
    })
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    skipButton.get.setTypeface(typeface)
    nextButton.get.setTypeface(typeface)
    doneButton.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    //we don't want to change default background color here, so no call to super
  }

  private[this] def onSkipPressed(v: View) = {
    onBackPressed()
  }

  private[this] def onNextPressed(v: View) = {
    viewPager.get.setCurrentItem(viewPager.get.getCurrentItem + 1)
  }

  private[this] def onDonePressed(v: View) = {
    onBackPressed()
  }
}
