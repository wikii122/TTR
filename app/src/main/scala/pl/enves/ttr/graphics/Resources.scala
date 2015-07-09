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

import android.content.Context
import android.graphics.{BitmapFactory, Bitmap}
import android.opengl.{GLUtils, GLES20}
import pl.enves.androidx.Logging
import pl.enves.ttr.R
import pl.enves.ttr.graphics.models._
import pl.enves.ttr.graphics.shaders._

import scala.collection.mutable

class Resources(context: Context) extends Logging {

  object ModelId extends Enumeration {
    type ModelId = Value
    val Rectangle, Board3x3 = Value
  }

  object TextureId extends Enumeration {
    type TextureId = Value
    val ArrowLeft, ArrowLeftGray, ArrowRight, ArrowRightGray, Ring, Cross = Value
  }

  object ShaderId extends Enumeration {
    type ShaderId = Value
    val Color, Colors, Texture = Value
  }

  val squareVBOs = new VBOs(
    createFloatBuffer(Square.coords),
    0,
    0,
    createFloatBuffer(unflipY(Square.texCoords))
  )

  val squareGeometry = new GeometryArrays(
    Square.numVertex,
    GLES20.GL_TRIANGLE_STRIP,
    squareVBOs
  )

  val board3x3VBOs = new VBOs(
    createFloatBuffer(Board3x3.coords),
    createFloatBuffer(Board3x3.colors),
    0,
    0
  )

  val board3x3Geometry = new GeometryElements(
    Board3x3.indices.length,
    createShortBuffer(Board3x3.indices),
    GLES20.GL_LINES,
    board3x3VBOs
  )

  //create models
  var models: mutable.HashMap[String, Geometry] = mutable.HashMap(
    (ModelId.Rectangle.toString, squareGeometry),
    (ModelId.Board3x3.toString, board3x3Geometry)
  )

  def addProcModel(name: String, procGeometry: ProcGeometry): Unit = {
    log("adding model: " + name)
    val buffers = procGeometry.getBuffers
    val vbos = new VBOs(
      if(buffers.positions.isDefined) createFloatBuffer(buffers.positions.get) else 0,
      if(buffers.colors.isDefined) createFloatBuffer(buffers.colors.get) else 0,
      if(buffers.normals.isDefined) createFloatBuffer(buffers.normals.get) else 0,
      if(buffers.texCoords.isDefined) createFloatBuffer(unflipY(buffers.texCoords.get)) else 0
    )
    val geometry = procGeometry match {
      case ProcGeometryArrays(numVertices, drawMode, b) => new GeometryArrays(
        numVertices,
        drawMode,
        vbos)
      case ProcGeometryElements(numIndices, indices, drawMode, b) => new GeometryElements(
        numIndices,
        createShortBuffer(indices),
        drawMode,
        vbos)
    }
    models.update(name, geometry)
  }

  var textures: mutable.HashMap[String, Int] = mutable.HashMap(
    (TextureId.ArrowLeft.toString, createTexture(context, R.drawable.arrow_left)),
    (TextureId.ArrowRight.toString, createTexture(context, R.drawable.arrow_right)),
    (TextureId.ArrowLeftGray.toString, createTexture(context, R.drawable.arrow_left_gray)),
    (TextureId.ArrowRightGray.toString, createTexture(context, R.drawable.arrow_right_gray)),
    (TextureId.Ring.toString, createTexture(context, R.drawable.ring)),
    (TextureId.Cross.toString, createTexture(context, R.drawable.cross))
  )

  def addProcTexture(name: String, bitmap: Bitmap): Unit = {
    log("adding texture: " + name)
    textures.update(name, createTexture(bitmap))
  }

  //create shaders
  var shaders = Map(
    (ShaderId.Color, new ColorShader()),
    (ShaderId.Colors, new ColorsShader()),
    (ShaderId.Texture, new TextureShader())
  )

  def getTexture(texture: TextureId.TextureId): Int = textures(texture.toString)

  def getTexture(texture: String): Int = textures(texture)

  def getGeometry(model: ModelId.ModelId): Geometry = models(model.toString)

  def getGeometry(model: String): Geometry = models(model)

  def getShader(shader: ShaderId.ShaderId): Shader = shaders(shader)

  // Buffer in GPU memory
  def createFloatBuffer(arr: Array[Float]): Int = {
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

  def createShortBuffer(arr: Array[Short]): Int = {
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

  def createTexture(bitmap: Bitmap): Int = {
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

  def createTexture(context: Context, image: Int): Int =
    createTexture(BitmapFactory.decodeResource(context.getResources, image))

  def unflipY(arr: Array[Float]): Array[Float] = {
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
  def apply(context: Context) = new Resources(context)
}