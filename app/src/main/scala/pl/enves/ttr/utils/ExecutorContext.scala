package pl.enves.ttr.utils

import android.os.AsyncTask

import scala.concurrent.ExecutionContext

object ExecutorContext {
  implicit val context = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR).asInstanceOf[ExecutionContext]
}
