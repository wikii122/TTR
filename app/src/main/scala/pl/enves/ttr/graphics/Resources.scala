package pl.enves.ttr.graphics

/**
 * Objects may utilize different combinations of per-vertex data, textures and shaders
 * which should exist in only one instance - that's what for this class is.
 *
 * TODO: make it better
 */

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

import android.graphics.Bitmap
import android.opengl.{GLUtils, GLES20}
import pl.enves.androidx.Logging

import pl.enves.ttr.graphics.shaders._

import scala.collection.mutable

object ShaderId extends Enumeration {
  type ShaderId = Value
  val Color, Colors, Texture = Value
}

class Resources() extends Logging {

  private val bitmapProviders: mutable.ListBuffer[BitmapProvider] = mutable.ListBuffer()
  private val geometryProviders: mutable.ListBuffer[GeometryProvider] = mutable.ListBuffer()


  def createOpenGLResources(): Unit = {
    log("Creating OpenGL Resources")
    //create textures
    for (bitmapProvider <- bitmapProviders) {
      log("Polling Bitmap Provider: " + bitmapProvider)
      val names = bitmapProvider.getBitmapsNames
      for (name <- names) {
        log("Adding Bitmap: " + name)
        addTexture(name, bitmapProvider.getBitmap(name))
      }
    }

    //create geometries
    for (geometryProvider <- geometryProviders) {
      log("Polling Geometry Provider: " + geometryProvider)
      for (geometry <- geometryProvider.getGeometry) {
        log("Adding Geometry: " + geometry._1)
        addModel(geometry._1, geometry._2)
      }
    }

    //create shaders
    shaders = Map(
      (ShaderId.Color, new ColorShader()),
      (ShaderId.Colors, new ColorsShader()),
      (ShaderId.Texture, new TextureShader())
    )
  }

  def addBitmapProvider(provider: BitmapProvider): Unit = {
    bitmapProviders.append(provider)
    log("Added Bitmap Provider: " + provider.getClass.getName)
  }

  def addGeometryProvider(provider: GeometryProvider): Unit = {
    geometryProviders.append(provider)
    log("Added Geometry Provider: " + provider.getClass.getName)
  }

  //create models
  private val models: mutable.HashMap[String, Geometry] = mutable.HashMap()

  private def addModel(name: String, geometryData: GeometryData): Unit = {
    log("adding model: " + name)
    val buffers: BuffersData = geometryData.getBuffers
    val vbos = new VBOs(
      if (buffers.positions.isDefined) createFloatBuffer(buffers.positions.get) else 0,
      if (buffers.colors.isDefined) createFloatBuffer(buffers.colors.get) else 0,
      if (buffers.normals.isDefined) createFloatBuffer(buffers.normals.get) else 0,
      if (buffers.texCoords.isDefined) createFloatBuffer(unflipY(buffers.texCoords.get)) else 0
    )
    val geometry = geometryData match {
      case ArraysGeometryData(numVertices, drawMode, b) => new GeometryArrays(
        numVertices,
        drawMode,
        vbos)
      case ElementsGeometryData(numIndices, indices, drawMode, b) => new GeometryElements(
        numIndices,
        createShortBuffer(indices),
        drawMode,
        vbos)
    }
    models.update(name, geometry)
  }

  private val textures: mutable.HashMap[String, Int] = mutable.HashMap()

  private def addTexture(name: String, bitmap: Bitmap): Unit = {
    log("adding texture: " + name)
    textures.update(name, createTexture(bitmap))
  }

  private var shaders: Map[ShaderId.ShaderId, Shader] = Map()

//  def getTexture(texture: TextureId.TextureId): Int = textures(texture.toString)

  def getTexture(texture: String): Int = textures(texture)

  //def getGeometry(model: DefaultGeometryId.ModelId): Geometry = models(model.toString)

  def getGeometry(model: String): Geometry = models(model)

  def getShader(shader: ShaderId.ShaderId): Shader = shaders(shader)

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

  private def createShortBuffer(arr: Array[Short]): Int = {
    val shortBuffer = ByteBuffer.allocateDirect(arr.length * 2)
      .order(ByteOrder.nativeOrder()).asShortBuffer()

    shortBuffer.put(arr).position(0)

    val name = IntBuffer.allocate(1)
    GLES20.glGenBuffers(1, name)
    val buffer = name.get(0)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer)

    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, arr.length * 2, shortBuffer, GLES20.GL_STATIC_DRAW)

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    return buffer
  }

  private def createTexture(bitmap: Bitmap): Int = {
    val name = IntBuffer.allocate(1)
    GLES20.glGenTextures(1, name)
    val texture = name.get(0)

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST)
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    // Recycle the bitmap, since its data has been loaded into OpenGL.
    bitmap.recycle()

    return texture
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

object Resources {
  def apply() = new Resources()
}