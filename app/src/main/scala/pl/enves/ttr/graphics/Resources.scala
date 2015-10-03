package pl.enves.ttr.graphics

/**
 * Objects may utilize different combinations of per-vertex data, textures and shaders
 * which should exist in only one instance - that's what for this class is.
 *
 * TODO: make it better
 */

import java.nio.{ByteBuffer, ByteOrder, IntBuffer}

import android.content.res.AssetManager
import android.graphics.Typeface
import android.opengl.GLES20
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics.shaders._
import pl.enves.ttr.utils.themes.Theme

import scala.collection.mutable

class Resources(assetManager: AssetManager) extends Logging {

  private val textureProviders: mutable.ListBuffer[TextureProvider] = mutable.ListBuffer()
  private val geometryProviders: mutable.ListBuffer[GeometryProvider] = mutable.ListBuffer()

  private val models: mutable.HashMap[String, Geometry] = mutable.HashMap()
  
  private val textures: mutable.HashMap[String, Int] = mutable.HashMap()

  private var maskShader: Option[MaskShader] = None
  
  private var _theme: Option[Theme] = None

  private val typeFace = Typeface.createFromAsset(assetManager, "fonts/comfortaa.ttf")

  def createOpenGLResources(): Unit = {
    log("Creating OpenGL Resources")
    //create textures
    for (textureProvider <- textureProviders) {
      log("Polling Texture Provider: " + textureProvider)
      for (texture <- textureProvider.getTextures) {
        log("Adding Texture: " + texture._1)
        addTexture(texture._1, texture._2)
      }
    }

    //create geometries
    for (geometryProvider <- geometryProviders) {
      log("Polling Geometry Provider: " + geometryProvider)
      for (geometry <- geometryProvider.getGeometry) {
        log("Adding Geometry: " + geometry._1)
        addGeometry(geometry._1, geometry._2)
      }
    }

    //create shaders
    maskShader = Some(new MaskShader())
  }

  def addTextureProvider(provider: TextureProvider): Unit = {
    textureProviders.append(provider)
    log("Added Texture Provider: " + provider.getClass.getName)
  }

  def addGeometryProvider(provider: GeometryProvider): Unit = {
    geometryProviders.append(provider)
    log("Added Geometry Provider: " + provider.getClass.getName)
  }

  private def addGeometry(name: String, geometryData: GeometryData): Unit = {
    val buffers = geometryData.buffers
    val buffersGpu = new Buffers[Int] (
      createFloatBuffer(buffers.positions),
      createFloatBuffer(unflipY(buffers.texCoords))
    )
    val geometry = new Geometry(
        geometryData.buffers.positions.length,
        geometryData.drawMode,
        buffersGpu)
    models.update(name, geometry)
  }

  private def addTexture(name: String, texture: Int): Unit = {
    textures.update(name, texture)
  }

  def getTexture(texture: String): Int = textures(texture)

  def getGeometry(model: String): Geometry = models(model)

  def getMaskShader: MaskShader = maskShader.get

  def getTheme: Theme = _theme.get

  def setTheme(theme: Theme): Unit = _theme = Some(theme)

  def getTypeFace: Typeface = typeFace

  // Buffer in GPU memory
  private def createFloatBuffer(arr: Array[Float]): Int = {
    val floatBuffer = ByteBuffer.allocateDirect(arr.length * 4)
      .order(ByteOrder.nativeOrder()).asFloatBuffer()

    floatBuffer.put(arr).position(0)

    val name = IntBuffer.allocate(1)
    GLES20.glGenBuffers(1, name)
    val buffer = name.get(0)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer)

    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arr.length * 4, floatBuffer, GLES20.GL_STATIC_DRAW)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    return buffer
  }

  private def unflipY(arr: Array[Float]): Array[Float] = {
    val ret = new Array[Float](arr.length)
    for (i <- arr.indices) {
      if (i % 2 == 0) {
        ret(i) = arr(i)
      } else {
        ret(i) = 1.0f - arr(i)
      }
    }
    return ret
  }
}
