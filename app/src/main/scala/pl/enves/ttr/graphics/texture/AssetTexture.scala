package pl.enves.ttr.graphics.texture

import java.io.InputStream

import android.content.Context
import android.graphics.BitmapFactory
import pl.enves.androidx.IOUtils
import pl.enves.ttr.graphics.TextureProvider

class AssetTexture(context: Context, path: String) extends TextureProvider {

  def decode(path: String): Int = {
    val stream: InputStream = context.getAssets.open(path)
    val rawData = IOUtils.readBytes(stream)
    stream.close()

    val bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length)

    val texture = createTexture(bitmap)
    bitmap.recycle()

    return texture
  }

  override def getTexture: Int = decode(path)
}
