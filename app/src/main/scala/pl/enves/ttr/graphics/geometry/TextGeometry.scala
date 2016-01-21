package pl.enves.ttr.graphics.geometry

import android.opengl.GLES20
import pl.enves.ttr.graphics.texture.CharactersTexture
import pl.enves.ttr.utils.Triangle

class TextGeometry(text: String, characters: CharactersTexture) extends Geometry {

  //TODO: breaking
  private[this] var positions = Array[Float]()
  private[this] var texCoords = Array[Float]()
  private[this] var pos = 0.0f
  private[this] val height = characters.getNormalizedFontHeight
  for (c <- text) {
    val (x, y) = characters.getNormalizedCoordinates(c)
    val width = characters.getNormalizedWidth(c)
    positions = positions ++ Rectangle.positionsCenterYTriangles(pos, 0.0f, width, height)
    texCoords = texCoords ++ Rectangle.texCoordinatesTriangles(x, y, width, height)
    pos = pos + width
  }

  private[this] val buffersGpu = new Buffers[Int](
    createFloatBuffer(positions),
    createFloatBuffer(unflipY(texCoords))
  )

  private[this] val numVertices = positions.length / 3

  override def getNumVertices: Int = numVertices

  override def getDrawMode: Int = GLES20.GL_TRIANGLES

  override def getBuffers: Buffers[Int] = buffersGpu

  override def getBoundingFigure: Array[Triangle] = Array[Triangle]() //TODO

  def getWidth: Float = pos

  def getHeight: Float = height
}
