package pl.enves.ttr.graphics.geometry

import android.graphics.RectF
import pl.enves.ttr.graphics.texture.CharactersTexture
import pl.enves.ttr.utils.math.MeshTriangle

object TextGeometry {
  def apply(text: String, characters: CharactersTexture): Geometry = {

    //TODO: breaking
    var model = List[MeshTriangle]()
    var currentY = 0.0f
    val height = 1.0f
    for (c <- text) {
      val (x, y) = characters.getNormalizedCoordinates(c)
      val textureCellWidth = characters.getNormalizedWidth(c)
      val textureCellHeight = characters.getNormalizedFontHeight
      val charWidth = height * (textureCellWidth / textureCellHeight)
      val position = new RectF(currentY, -height / 2, currentY + charWidth, height / 2)
      val texCoord = new RectF(x, y, x + textureCellWidth, y + textureCellHeight)
      model ++= MeshRectangle2D(position, texCoord)
      currentY += charWidth
    }

    return Geometry(model)
  }
}
