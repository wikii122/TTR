package pl.enves.androidx

import java.io.{ByteArrayOutputStream, InputStream}

object IOUtils {
  def readBytes(is: InputStream): Array[Byte] = {
    val os: ByteArrayOutputStream = new ByteArrayOutputStream(1024)
    val buffer = new Array[Byte](1024)
    var len = is.read(buffer)
    while (len >= 0) {
      os.write(buffer, 0, len)
      len = is.read(buffer)
    }
    return os.toByteArray
  }
}
