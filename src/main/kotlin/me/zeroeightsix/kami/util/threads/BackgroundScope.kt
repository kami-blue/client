package me.zeroeightsix.kami.util.threads

import kotlinx.coroutines.*
import me.zeroeightsix.kami.KamiMod

@Suppress("EXPERIMENTAL_API_USAGE")
internal object BackgroundScope : CoroutineScope by CoroutineScope(newFixedThreadPoolContext(2, "KAMI Blue Background")) {

    private val jobs = LinkedHashMap<BackgroundJob, Job?>()
    private var started = false

    fun start() {
        started = true
        for ((job, _) in jobs) {
            jobs[job] = startJob(job)
        }
    }

    fun launchLooping(name: String, delay: Long, block: suspend CoroutineScope.() -> Unit) {
        launchLooping(BackgroundJob(name, delay, block))
    }

    fun launchLooping(job: BackgroundJob) {
        if (!started) {
            jobs[job] = null
        } else {
            jobs[job] = startJob(job)
        }
    }

    fun cancel(job: BackgroundJob) = jobs.remove(job)?.cancel()

    private fun startJob(job: BackgroundJob): Job {
        return launch {
            while (isActive) {
                try {
                    job.block(this)
                } catch (e: Exception) {
                    KamiMod.LOG.warn("Error occurred while running background job ${job.name}", e)
                }
                delay(job.delay)
            }
        }
    }

}