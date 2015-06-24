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
import pl.enves.ttr.R
import pl.enves.ttr.graphics.models._
import pl.enves.ttr.graphics.shaders._

class Resources(context: Context) {

  object ModelId extends Enumeration {
    type ModelId = Value
    val Rectangle, Board3x3 = Value
  }

  object TextureId extends Enumeration {
    type TextureId = Value
    val ArrowLeft, ArrowRight, Ring, Cross = Value
  }

  object ShaderId extends Enumeration {
    type ShaderId = Value
    val Color, Colors, Texture = Value
  }

  val squareVBOs = new VBOs(
    createFloatBuffer(Square.coords),
    createFloatBuffer(Square.colors),
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
  var models = Map(
    (ModelId.Rectangle, squareGeometry),
    (ModelId.Board3x3, board3x3Geometry)
  )

  //create textures
  var textures = Map(
    (TextureId.ArrowLeft, createTexture(R.drawable.arrow_left)),
    (TextureId.ArrowRight, createTexture(R.drawable.arrow_right)),
    (TextureId.Ring, createTexture(R.drawable.ring)),
    (TextureId.Cross, createTexture(R.drawable.cross))
  )

  //create shaders
  var shaders = Map(
    (ShaderId.Color, new ColorShader()),
    (ShaderId.Colors, new ColorsShader()),
    (ShaderId.Texture, new TextureShader())
  )

  def getTexture(texture: TextureId.TextureId): Int = textures(texture)

  def getGeometry(model: ModelId.ModelId): Geometry = models(model)

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

  def createTexture(image: Int): Int = {
    val bitmap: Bitmap = BitmapFactory.decodeResource(context.getResources, image)

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