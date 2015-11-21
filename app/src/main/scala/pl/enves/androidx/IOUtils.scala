package pl.enves.androidx

import java.io.{InputStreamReader, BufferedReader, ByteArrayOutputStream, InputStream}

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

  def readText(is: InputStream): String = {
    val builder = new StringBuilder()
    val br = new BufferedReader(new InputStreamReader(is, "UTF-8"))
    var str = br.readLine()

    while (str != null) {
      builder.append(str)
      builder.append('\n')
      str = br.readLine()
    }

    br.close()
    return builder.result()
  }
}
