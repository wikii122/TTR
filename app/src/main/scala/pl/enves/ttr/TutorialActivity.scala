package pl.enves.ttr

import java.util

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.{BitmapFactory, Typeface}
import android.os.{Bundle, Handler}
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view._
import android.widget.{Button, ImageView, TextView}
import org.xmlpull.v1.{XmlPullParser, XmlPullParserException}
import pl.enves.androidx.helpers._
import pl.enves.androidx.{ExtendedActivity, Logging}

abstract class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected val fontPath = "fonts/comfortaa.ttf"

  protected def changeFont(view: View, id: Int, typeface: Typeface): Unit = {
    val textView = find[TextView](view, id)
    textView.setTypeface(typeface)
  }

  protected def changeText(view: View, id: Int, textId: Int): Unit = {
    val textView = find[TextView](view, id)
    textView.setText(textId)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, args: Bundle): View = {
    val view: View = inflater.inflate(layout, container, false)

    setupView(view)

    return view
  }

  protected def setupView(view: View): Unit = {}

  def onSelected(): Unit = {}

  def onDeSelected(): Unit = {}

  protected val layout: Int = R.layout.fragment_tutorial

  protected val title: Int = R.id.tutorial_title

  protected val text: Int = R.id.tutorial_text

  protected val image: Int = R.id.tutorial_image
}

class ImageFragment(titleRes: Int, textRes: Int, imageRes: Int) extends ExtendedFragment {
  override def setupView(view: View): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeText(view, title, titleRes)
    changeText(view, text, textRes)
    changeFont(view, title, typeface)
    changeFont(view, text, typeface)
    val imageView = find[ImageView](view, image)
    imageView.setImageResource(imageRes)
  }
}

class AnimationFragment(titleRes: Int, textRes: Int, animationRes: Int) extends ExtendedFragment with Logging {
  private val frames: util.ArrayList[MyFrame] = new util.ArrayList[MyFrame]()

  private var animate = false

  override def setupView(view: View): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getContext.getAssets, fontPath)
    changeText(view, title, titleRes)
    changeText(view, text, textRes)
    changeFont(view, title, typeface)
    changeFont(view, text, typeface)

    loadAnimation(animationRes, getContext)
  }

  override def onSelected(): Unit = {
    log("onSelected")
    val imageView = find[ImageView](getView, image)
    startAnimation(imageView)
  }

  override def onDeSelected(): Unit = {
    log("onDeSelected")
    val imageView = find[ImageView](getView, image)
    stopAnimation(imageView)
  }

  override def onStart(): Unit = {
    super.onStart()
    if (getArguments != null) {
      if (getArguments.getBoolean("AUTOPLAY", false)) {
        onSelected()
      }
    }
  }

  override def onStop(): Unit = {
    super.onStop()
    log("onStop")
    onDeSelected()
  }

  class MyFrame(drawableId: Int, duration: Int) {
    private var drawable: Option[Drawable] = None

    def getDuration = duration

    def isReady = {
      drawable.synchronized {
        drawable.isDefined
      }
    }

    def getDrawable = {
      drawable.synchronized {
        drawable.get
      }
    }

    def makeDrawable(context: Context): Unit = {
      drawable.synchronized {
        val resources = context.getResources
        val d = new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, drawableId))
        drawable = Some(d)
      }
    }

    def discardDrawable(): Unit = {
      drawable.synchronized {
        if (drawable.isDefined) {
          drawable.get.asInstanceOf[BitmapDrawable].getBitmap.recycle()
          drawable = None
        }
      }
    }
  }

  def loadAnimation(resourceId: Int, context: Context): Unit = {
    val parser: XmlResourceParser = context.getResources.getXml(resourceId)
    frames.clear()
    try {
      var eventType = parser.getEventType
      while (eventType != XmlPullParser.END_DOCUMENT) {
        eventType match {
          case XmlPullParser.START_DOCUMENT =>
          case XmlPullParser.START_TAG =>

            if (parser.getName.equals("item")) {
              var drawableId = 0
              var duration = 0

              for (i <- 0 until parser.getAttributeCount) {
                if (parser.getAttributeName(i).equals("drawable")) {
                  drawableId = Integer.parseInt(parser.getAttributeValue(i).substring(1))
                }
                else if (parser.getAttributeName(i).equals("duration")) {
                  duration = parser.getAttributeIntValue(i, 1000)
                }
              }

              val myFrame = new MyFrame(drawableId, duration)
              frames.add(myFrame)
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

  def startAnimation(imageView: ImageView): Unit = {
    frames.synchronized {
      log("starting")
      if (!animate) {
        animate = true

        val thisFrame = frames.get(0)
        thisFrame.makeDrawable(imageView.getContext)
        imageView.setImageDrawable(thisFrame.getDrawable)

        val nextFrame = frames.get(1)
        nextFrame.makeDrawable(imageView.getContext)

        new Handler().postDelayed(new Runnable() {
          override def run(): Unit = {
            animateNext(imageView, 1)
          }
        }, thisFrame.getDuration)
      }
    }
  }

  def stopAnimation(imageView: ImageView): Unit = {
    frames.synchronized {
      log("stopping")
      animate = false
      val current = imageView.getDrawable
      for (i <- 0 until frames.size()) {
        val frame = frames.get(i)
        if (frame.isReady) {
          if (frame.getDrawable != current) {
            frame.discardDrawable()
          }
        }
      }
    }
  }

  def animateNext(imageView: ImageView, frameNumber: Int) {
    if (animate) {
      val prevFrameNumber = if (frameNumber > 0) frameNumber - 1 else frames.size() - 1
      val nextFrameNumber = if (frameNumber < frames.size() - 1) frameNumber + 1 else 0

      val thisFrame = frames.get(frameNumber)
      log("setting image " + frameNumber)
      imageView.setImageDrawable(thisFrame.getDrawable)

      val prevFrame = frames.get(prevFrameNumber)
      prevFrame.discardDrawable()

      val nextFrame = frames.get(nextFrameNumber)
      nextFrame.makeDrawable(imageView.getContext)

      new Handler().postDelayed(new Runnable() {
        override def run(): Unit = {
          frames.synchronized {
            animateNext(imageView, nextFrameNumber)
          }
        }
      }, thisFrame.getDuration)
    }
  }
}

class TutorialFragmentPagerAdapter(fm: FragmentManager, context: Context) extends FragmentPagerAdapter(fm) with Logging {
  val items = Array(
    new AnimationFragment(R.string.tutorial_figures_title, R.string.tutorial_figures, R.drawable.tutorial_figures_animation),
    new AnimationFragment(R.string.tutorial_rotations_title, R.string.tutorial_rotations, R.drawable.tutorial_rotations_animation),
    new AnimationFragment(R.string.tutorial_goals_title, R.string.tutorial_goals, R.drawable.tutorial_goals_animation),
    new ImageFragment(R.string.tutorial_standard_title, R.string.tutorial_standard, R.drawable.tutorial_placeholder),
    new ImageFragment(R.string.tutorial_network_title, R.string.tutorial_network, R.drawable.tutorial_placeholder)
  )

  val args: Bundle = new Bundle()
  args.putBoolean("AUTOPLAY", true)
  items(0).setArguments(args)

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
        adapter.get.getItem(currentFragment).asInstanceOf[ExtendedFragment].onDeSelected()
        currentFragment = position
        adapter.get.getItem(position).asInstanceOf[ExtendedFragment].onSelected()
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
