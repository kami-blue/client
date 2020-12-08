package org.kamiblue.commons.utils

import java.util.concurrent.Executors
import java.util.concurrent.Future

object ThreadUtils {
    private val threadPool = Executors.newCachedThreadPool()

    fun submitTask(task: Runnable): Future<*> = threadPool.submit(task)
}