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
import pl.enves.androidx.helpers._
import pl.enves.androidx.{ExtendedFragment, ExtendedActivity, IOUtils, Logging}

class DoubleTextFragment extends ExtendedFragment with Logging {
  private var text1Res = 0
  private var text2Res = 0

  override protected val layout: Int = R.layout.fragment_tutorial_double_text

  protected val text1: Int = R.id.tutorial_text_1

  protected val text2: Int = R.id.tutorial_text_2

  override def onStart(): Unit = {
    super.onStart()

    text1Res = getArguments.getInt("TEXT_1_RES", 0)
    text2Res = getArguments.getInt("TEXT_2_RES", 0)

    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeText(getView, text1, text1Res)
    changeText(getView, text2, text2Res)
    changeFont(getView, text1, typeface)
    changeFont(getView, text2, typeface)
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

class AnimationFragment extends ExtendedFragment with Logging {

  private var textRes = 0
  private var animationRes = 0

  override protected val layout: Int = R.layout.fragment_tutorial_image_text

  protected val text: Int = R.id.tutorial_text

  protected val image: Int = R.id.tutorial_image

  private val frameSpecs: util.ArrayList[FrameSpec] = new util.ArrayList[FrameSpec]()

  private val framesLoaded = new LinkedBlockingQueue[FrameData](2)

  private var frameToLoad = 0

  private var producerThread: Option[Thread] = None
  private var consumerThread: Option[Thread] = None

  private var lastFrameData: Option[FrameData] = None

  private var animate = false

  private var autoPlay = false

  private var wasAutoPlayed = false

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    log("onCreate" + number)
    if (savedInstanceState != null) {
      autoPlay = savedInstanceState.getBoolean("AUTO_PLAY", false)
      wasAutoPlayed = false
    }
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    log("onSaveInstanceState" + number)
    outState.putBoolean("AUTO_PLAY", animate)
  }

  override def onStart(): Unit = {
    super.onStart()
    log("onStart" + number)

    textRes = getArguments.getInt("TEXT_RES", 0)
    animationRes = getArguments.getInt("ANIMATION_RES", 0)

    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeText(getView, text, textRes)
    changeFont(getView, text, typeface)

    loadAnimation(animationRes, getContext)
    val imageView = find[ImageView](getView, image)
    displayFirstFrame(imageView, getContext)
    autoPlay = autoPlay || getArguments.getBoolean("AUTO_PLAY", false)
    if (autoPlay && !wasAutoPlayed) {
      wasAutoPlayed = true
      startAnimation(imageView, getContext)
    }
  }

  override def onSelected(): Unit = {
    log("onSelected" + number)
    val imageView = find[ImageView](getView, image)
    startAnimation(imageView, getContext)
  }

  override def onDeSelected(): Unit = {
    log("onDeSelected" + number)
    stopAnimation()
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

class TutorialActivity extends ExtendedActivity {
  var adapter: Option[TutorialFragmentPagerAdapter] = None

  var currentFragment = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tutorial_layout)

    val viewPager: ViewPager = find[ViewPager](R.id.tutorial_viewpager)
    adapter = Some(new TutorialFragmentPagerAdapter(getSupportFragmentManager, TutorialActivity.this))
    viewPager.setAdapter(adapter.get)

    val skipButton = find[Button](R.id.tutorial_skip_button)
    skipButton onClick onSkipPressed

    val nextButton = find[Button](R.id.tutorial_next_button)
    nextButton onClick onNextPressed

    val doneButton = find[Button](R.id.tutorial_done_button)
    doneButton onClick onDonePressed

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}

      override def onPageSelected(position: Int): Unit = {
        if (position == adapter.get.getCount - 1) {
          skipButton.setVisibility(View.INVISIBLE)
          nextButton.setVisibility(View.GONE)
          doneButton.setVisibility(View.VISIBLE)
        } else {
          skipButton.setVisibility(View.VISIBLE)
          doneButton.setVisibility(View.GONE)
          nextButton.setVisibility(View.VISIBLE)
        }
        viewPager.getAdapter.instantiateItem(viewPager, currentFragment).asInstanceOf[ExtendedFragment].onDeSelected()
        currentFragment = position
        viewPager.getAdapter.instantiateItem(viewPager, currentFragment).asInstanceOf[ExtendedFragment].onSelected()
      }

      override def onPageScrollStateChanged(state: Int): Unit = {}
    })
  }

  override def onStart() = {
    super.onStart()

    setBottomBarGui()

    applyCustomFont("fonts/comfortaa.ttf")
  }

  override def onPause() {
    super.onPause()
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val skipButton = find[Button](R.id.tutorial_skip_button)
    skipButton.setTypeface(typeface)

    val nextButton = find[Button](R.id.tutorial_next_button)
    nextButton.setTypeface(typeface)

    val doneButton = find[Button](R.id.tutorial_done_button)
    doneButton.setTypeface(typeface)
  }

  private[this] def onSkipPressed(v: View) = {
    onBackPressed()
  }

  private[this] def onNextPressed(v: View) = {
    val viewPager: ViewPager = find[ViewPager](R.id.tutorial_viewpager)
    viewPager.setCurrentItem(viewPager.getCurrentItem + 1)
  }

  private[this] def onDonePressed(v: View) = {
    onBackPressed()
  }
}
