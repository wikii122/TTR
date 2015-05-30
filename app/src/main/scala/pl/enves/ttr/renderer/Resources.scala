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

  def ID_MODEL_TRIANGLE = 0

  def ID_MODEL_RECT = 1

  def ID_TEXTURE_TEST = 0

  def ID_SHADER_MANDEL = 0

  def ID_SHADER_COLOR = 1

  def ID_SHADER_TEXTURE = 2

  //create models
  var models = new Array[Model3d](2)
  models(ID_MODEL_TRIANGLE) = new Model3d(
    Triangle.numVertex,
    createFloatBuffer(Triangle.coords),
    createFloatBuffer(Triangle.colors),
    0,
    0)

  models(ID_MODEL_RECT) = new Model3d(Square.numVertex,
    createFloatBuffer(Square.coords),
    createFloatBuffer(Square.colors),
    0,
    0)

  GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

  //create textures
  //TODO
  var textures = new Array[Int](1)

  //create shaders
  var shaders = new Array[Shader](3)
  shaders(ID_SHADER_MANDEL) = new MandelShader()
  shaders(ID_SHADER_COLOR) = new ColorShader()

  //shaders(ID_SHADER_TEXTURE) = new TextureShader()


  // Texture is just a number
  def getTexture(texture: Int): Int = {
    textures(texture)
  }

  // per-vertex data
  def getModel3d(model: Int): Model3d = {
    models(model)
  }

  // But every shader needs some code around
  def getShader(shader: Int): Shader = {
    shaders(shader)
  }

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

    buffer
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

    buffer
  }
}
