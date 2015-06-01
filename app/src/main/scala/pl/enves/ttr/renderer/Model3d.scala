package pl.enves.ttr.renderer

/**
 * Class to store OpenGL buffers with per-vertex data
 */


case class Model3d(numVertex: Int,
                   positionsBuffer: Int,
                   colorsBuffer: Int,
                   normalsBuffer: Int,
                   texCoordsBuffer: Int) {
}
