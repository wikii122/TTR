package pl.enves.ttr.renderer

/**
 * Objects may utilize different combinations of per-vertex data, textures and shaders
 * which should exist in only one instance - that's what for this class is.
 *
 * TODO: make it better
 */

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.{BitmapFactory, Bitmap}
import android.opengl.{GLUtils, GLES20}
import pl.enves.ttr.R
import pl.enves.ttr.renderer.models._
import pl.enves.ttr.renderer.shaders._

class Resources(context: Context) {

  object ModelId extends Enumeration {
    type ModelId = Value
    val Triangle, Rectangle = Value
  }

  object TextureId extends Enumeration {
    type TextureId = Value
    val Test1, Test2 = Value
  }

  object ShaderId extends Enumeration {
    type ShaderId = Value
    val Mandel, Color, Texture = Value
  }

  //create models
  var models = Map(
    (ModelId.Triangle, new Model3d(
      Triangle.numVertex,
      createFloatBuffer(Triangle.coords),
      createFloatBuffer(Triangle.colors),
      0,
      createFloatBuffer(unflipY(Triangle.texCoords)))),
    (ModelId.Rectangle, new Model3d(
      Square.numVertex,
      createFloatBuffer(Square.coords),
      createFloatBuffer(Square.colors),
      0,
      createFloatBuffer(unflipY(Square.texCoords))))
  )

  //create textures
  //TODO
  var textures = Map(
    (TextureId.Test1, createTexture(R.drawable.sky)),
    (TextureId.Test2, createTexture(R.drawable.wood))
  )

  //create shaders
  var shaders = Map(
    (ShaderId.Mandel, new MandelShader()),
    (ShaderId.Color, new ColorShader()),
    (ShaderId.Texture, new TextureShader())
  )

  def getTexture(texture: TextureId.TextureId): Int = textures(texture)

  def getModel3d(model: ModelId.ModelId): Model3d = models(model)

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
    for(i <- arr.indices ) {
      if(i%2 == 0) {
        ret(i) = arr(i)
      } else {
        ret(i) = 1.0f-arr(i)
      }
    }
    return ret
  }
}

object Resources {
  def apply(context: Context) = new Resources(context)
}