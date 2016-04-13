package pl.enves.ttr.utils.tutorial

import java.io.{IOException, InputStream}
import java.util
import java.util.concurrent.LinkedBlockingQueue

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.{Bitmap, BitmapFactory, Typeface}
import android.os.Bundle
import android.view._
import android.widget.{ImageView, TextView}
import org.xmlpull.v1.{XmlPullParser, XmlPullParserException}
import pl.enves.androidx._
import pl.enves.ttr.R
import pl.enves.ttr.utils.styled.StyledFragment

class AnimationFragment extends StyledFragment with Selectable with Logging {
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

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    if (savedInstanceState != null) {
      autoPlay = savedInstanceState.getBoolean("AUTO_PLAY", false)
      wasAutoPlayed = false
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_tutorial_image_text, container, false)
    textView = Some(find[TextView](view, R.id.tutorial_text))
    imageView = Some(find[ImageView](view, R.id.tutorial_image))
    return view
  }

  override def onStart(): Unit = {
    super.onStart()

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
    startAnimation(imageView.get, getContext)
  }

  override def onDeSelected(): Unit = {
    stopAnimation()
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    outState.putBoolean("AUTO_PLAY", animate)
  }

  override def onStop(): Unit = {
    super.onStop()
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
  def apply(textRes: Int, animationRes: Int, autoPlay: Boolean): AnimationFragment = {
    val animationFragment = new AnimationFragment
    val args: Bundle = new Bundle()
    args.putInt("TEXT_RES", textRes)
    args.putInt("ANIMATION_RES", animationRes)
    args.putBoolean("AUTO_PLAY", autoPlay)
    animationFragment.setArguments(args)
    return animationFragment
  }
}