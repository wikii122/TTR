package pl.enves.ttr.graphics.texture

import java.io.{IOException, InputStream}

import android.content.Context
import android.graphics.BitmapFactory
import pl.enves.androidx.{IOUtils, Logging}
import pl.enves.ttr.graphics.TextureUtils

object AssetTexture extends TextureUtils with Logging {
  def apply(context: Context, path: String): Int = {
    try {
      val stream: InputStream = context.getAssets.open(path)
      val rawData = IOUtils.readBytes(stream)
      stream.close()

      val bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length)

      val texture = createTexture(bitmap)

      //bitmap is recycled in createTexture()

      return texture
    } catch {
      case e: IOException =>
        error(e.getMessage)
        return 0
    }
  }
}
