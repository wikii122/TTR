package pl.enves.ttr.graphics.geometry

import java.nio.{ByteBuffer, ByteOrder, IntBuffer}

import android.opengl.GLES20
import pl.enves.ttr.utils.math.{MeshTriangle, Triangle, Vector2, Vector3}

class Geometry(model: List[MeshTriangle]) {

  val numVertices: Int = model.length * 3

  val buffers = createBuffers(model)

  val boundaries = calculateBoundaries(model)

  val boundingFigure = createBoundingRect2D(boundaries)

  val dimensions = calculateDimensions(boundaries)

  def draw(): Unit = {
    GLES20.glDrawArrays(Geometry.DrawMode, 0, numVertices)
  }

  case class Boundaries(maxX: Float, minX: Float, maxY: Float, minY: Float, maxZ: Float, minZ: Float)

  private def calculateBoundaries(model: List[MeshTriangle]): Boundaries = {
    var maxX, minX, maxY, minY, maxZ, minZ: Float = 0.0f

    def checkPosition(pos: Vector3): Unit = {
      if (pos.x > maxX) maxX = pos.x
      if (pos.x < minX) minX = pos.x
      if (pos.y > maxY) maxY = pos.y
      if (pos.y < minY) minY = pos.y
      if (pos.z > maxZ) maxZ = pos.z
      if (pos.z < minZ) minZ = pos.z
    }

    for (triangle <- model) {
      checkPosition(triangle.v0.position)
      checkPosition(triangle.v1.position)
      checkPosition(triangle.v2.position)
    }

    return Boundaries(maxX, minX, maxY, minY, maxZ, minZ)
  }

  //TODO: 3D
  private def createBoundingRect2D(b: Boundaries): List[Triangle] = {
    return Rectangle(Vector3(b.minX, b.minY, b.minZ), Vector3(b.maxX, b.maxY, b.maxZ))
  }

  private def calculateDimensions(b: Boundaries) = Vector3(
    Math.abs(b.maxX - b.minX),
    Math.abs(b.maxY - b.minY),
    Math.abs(b.maxZ - b.minZ)
  )

  private def createBuffers(model: List[MeshTriangle]): Buffers = {
    var positions = Seq[Float]()
    var texCoords = Seq[Float]()

    for (triangle <- model) {
      positions ++= triangle.v0.position.toSeq3
      positions ++= triangle.v1.position.toSeq3
      positions ++= triangle.v2.position.toSeq3

      texCoords ++= unFlipY(triangle.v0.texCoord).toSeq2
      texCoords ++= unFlipY(triangle.v1.texCoord).toSeq2
      texCoords ++= unFlipY(triangle.v2.texCoord).toSeq2
    }

    return Buffers(createFloatBuffer(positions.toArray), createFloatBuffer(texCoords.toArray))
  }

  // Buffer in GPU memory
  private def createFloatBuffer(arr: Array[Float]): Int = {
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

  private def unFlipY(v: Vector2): Vector2 = {
    return Vector2(v.x, 1.0f - v.y)
  }
}

object Geometry {
  val DrawMode: Int = GLES20.GL_TRIANGLES

  val PositionSize: Int = 3
  val TexCoordSize: Int = 2

  def apply(model: List[MeshTriangle]) = new Geometry(model)
}
