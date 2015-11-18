package pl.enves.ttr.graphics.geometry

/**
 * Class to store buffers, arrays or VBOs, that belong to the same model
 */
case class Buffers[T](positions: T, texCoords: T)
