package pl.enves.androidx.helpers

trait FunctionHelper {
  protected implicit def UnitToUnit(f: => Unit): () => Unit = () => f
}
