package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.modules.client.ChatSetting
import me.zeroeightsix.kami.util.TaskState
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.collections.HashSet

object MessageManager : Manager() {
    private val mc = Wrapper.minecraft
    private val lockObject = Any()

    private val messageQueue = TreeSet<QueuedMessage>(Comparator.reverseOrder())
    private val packetSet = HashSet<CPacketChatMessage>()
    private val timer = TimerUtils.TickTimer()
    private var currentId = 0

    init {
        listener<PacketEvent.Send>(0) {
            if (it.packet !is CPacketChatMessage || packetSet.contains(it.packet)) return@listener
            it.cancel()
            addMessageToQueue(it.packet, it)
        }

        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@listener

            synchronized(lockObject) {
                if (messageQueue.isEmpty()) {
                    // Reset the current id so we don't reach the max 32 bit integer limit (although that is not likely to happen)
                    currentId = 0
                } else {
                    if (timer.tick((ChatSetting.delay.value * 1000.0f).toLong())) {
                        messageQueue.pollFirst()?.let {
                            mc.connection?.sendPacket(it.message)
                            packetSet.remove(it.message)
                            it.state.done = true
                        }
                    }

                    // Removes the low priority messages if it exceed the limit
                    while (messageQueue.size > ChatSetting.maxMessageQueueSize.value) {
                        messageQueue.pollLast()
                    }
                }
            }
        }
    }

    fun addMessageToQueue(message: String, source: Any, priority: Int = 0): TaskState {
        return addMessageToQueue(CPacketChatMessage(message), source, priority)
    }

    fun addMessageToQueue(message: CPacketChatMessage, source: Any, priority: Int = 0): TaskState {
        return QueuedMessage(currentId++, priority, source, message).let {
            messageQueue.add(it)
            packetSet.add(message)
            it.state
        }
    }

    private data class QueuedMessage(
            val id: Int,
            val priority: Int,
            val source: Any,
            val message: CPacketChatMessage,
            val state: TaskState = TaskState()
    ) : Comparable<QueuedMessage> {

        override fun compareTo(other: QueuedMessage): Int {
            val result = priority - other.priority
            return if (result != 0) result
            else other.id - id
        }

    }
}