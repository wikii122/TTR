package pl.enves.ttr.graphics.geometry

import android.opengl.GLES20
import pl.enves.androidx.Logging
import pl.enves.ttr.graphics.models.Rectangle
import pl.enves.ttr.graphics.texture.CharactersTexture
import pl.enves.ttr.graphics.{AbstractGeometry, GeometryProvider}

class TextGeometryProvider(text: String, characters: CharactersTexture) extends Logging with GeometryProvider {

  //TODO: breaking
  override def getGeometry: AbstractGeometry = {
    var positions = Array[Float]()
    var texCoords = Array[Float]()
    var pos = 0.0f
    val height = characters.getNormalizedFontHeight
    for (c <- text) {
      val (x, y) = characters.getNormalizedCoordinates(c)
      val width = characters.getNormalizedWidth(c)
      positions = positions ++ Rectangle.positionsCenterYTriangles(pos, 0.0f, width, height)
      texCoords = texCoords ++ Rectangle.texCoordinatesTriangles(x, y, width, height)
      pos = pos + width
    }
    val buffers = new Buffers[Array[Float]](
      positions, texCoords
    )
    return createTextGeometry(
      GLES20.GL_TRIANGLES,
      buffers,
      pos,
      height
    )
  }

  private def createTextGeometry(drawMode: Int, buffers: Buffers[Array[Float]], width: Float, height: Float): AbstractGeometry = {
    val buffersGpu = new Buffers[Int](
      createFloatBuffer(buffers.positions),
      createFloatBuffer(unflipY(buffers.texCoords))
    )
    return new TextGeometry(
      buffers.positions.length / 3,
      drawMode,
      buffersGpu,
      width,
      height)
  }
}
