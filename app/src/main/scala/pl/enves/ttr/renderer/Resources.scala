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

import android.opengl.GLES20
import pl.enves.ttr.renderer.models._
import pl.enves.ttr.renderer.shaders._

class Resources {

  object ModelId extends Enumeration {
    type ModelId = Value
    val Triangle, Rectangle = Value
  }

  object TextureId extends Enumeration {
    type TextureId = Value
    val Test = Value
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
      0)),
    (ModelId.Rectangle, new Model3d(
      Square.numVertex,
      createFloatBuffer(Square.coords),
      createFloatBuffer(Square.colors),
      0,
      0))
  )

  //create textures
  //TODO
  var textures = Map((TextureId.Test, 0))

  //create shaders
  var shaders = Map(
    (ShaderId.Mandel, new MandelShader()),
    (ShaderId.Color, new ColorShader())
    //(ShaderId.Texture, new TextureShader())
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
}

object Resources {
  def apply() = new Resources
}