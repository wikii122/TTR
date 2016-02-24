package pl.enves.ttr.utils.tutorial

trait Selectable {
  def onSelected(): Unit = {}

  def onDeSelected(): Unit = {}
}
