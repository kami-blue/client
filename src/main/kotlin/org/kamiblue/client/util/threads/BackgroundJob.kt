package org.kamiblue.client.util.threads

import kotlinx.coroutines.CoroutineScope

class BackgroundJob(
    val name: String,
    val delay: Long,
    val block: suspend CoroutineScope.() -> Unit
) {

    override fun equals(other: Any?) = this === other
        || (other is BackgroundJob
        && name == other.name
        && delay == other.delay)

    override fun hashCode() = 31 * name.hashCode() + delay.hashCode()

}