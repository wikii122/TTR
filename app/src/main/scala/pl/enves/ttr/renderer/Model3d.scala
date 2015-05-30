package pl.enves.ttr.renderer

/**
 * Class to store OpenGL buffers with per-vertex data
 */


class Model3d(numVertex: Int,
              positionsBuffer: Int,
              colorsBuffer: Int,
              normalsBuffer: Int,
              texCoordsBuffer: Int) {

  def getNumVertex: Int = {
    numVertex
  }

  def getPositionsBuffer: Int = {
    positionsBuffer
  }

  def getColorsBuffer: Int = {
    colorsBuffer
  }

  def getNormalsBuffer: Int = {
    normalsBuffer
  }

  def getTexCoordsBuffer: Int = {
    texCoordsBuffer
  }
}
