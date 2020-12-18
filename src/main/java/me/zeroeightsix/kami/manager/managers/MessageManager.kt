package me.zeroeightsix.kami.manager.managers

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.mixin.extension.packetMessage
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.ChatSetting
import me.zeroeightsix.kami.util.TaskState
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.*
import kotlin.collections.HashSet

object MessageManager : Manager {
    private val mc = Wrapper.minecraft
    private val lockObject = Any()

    private val messageQueue = TreeSet<QueuedMessage>(Comparator.reverseOrder())
    private val packetSet = HashSet<CPacketChatMessage>()
    private val timer = TimerUtils.TickTimer()
    var lastPlayerMessage = ""
    private var currentId = 0

    private val activeModifiers = TreeSet<MessageModifier>(Comparator.reverseOrder())
    private var modifierId = 0

    init {
        listener<PacketEvent.Send>(0) {
            if (it.packet !is CPacketChatMessage || packetSet.remove(it.packet)) return@listener
            it.cancel()
            if (it.packet.message != lastPlayerMessage) addMessageToQueue(it.packet, it)
            else addMessageToQueue(it.packet, mc.player?: it, Int.MAX_VALUE - 1)
        }

        listener<SafeTickEvent>(-69420) { event ->
            if (event.phase != TickEvent.Phase.START) return@listener

            synchronized(lockObject) {
                if (messageQueue.isEmpty()) {
                    // Reset the current id so we don't reach the max 32 bit integer limit (although that is not likely to happen)
                    currentId = 0
                } else {
                    if (timer.tick((ChatSetting.delay.value * 1000.0f).toLong())) {
                        messageQueue.pollFirst()?.let {
                            for (modifier in activeModifiers) modifier.apply(it)
                            if (it.packet.message.isNotBlank()) mc.connection?.sendPacket(it.packet)
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

    fun sendMessageDirect(message: String) {
        val packet = CPacketChatMessage(message)
        packetSet.add(packet)
        mc.connection?.sendPacket(packet)
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

    data class QueuedMessage(
            private val id: Int,
            private val priority: Int,
            val source: Any,
            val packet: CPacketChatMessage,
            val state: TaskState = TaskState()
    ) : Comparable<QueuedMessage> {

        override fun compareTo(other: QueuedMessage): Int {
            val result = priority - other.priority
            return if (result != 0) result
            else other.id - id
        }

    }

    fun Module.newMessageModifier(filter: (QueuedMessage) -> Boolean = { true }, modifier: (QueuedMessage) -> String) =
            MessageModifier(modifierId++, modulePriority, filter, modifier)

    class MessageModifier(
            private val id: Int,
            private val priority: Int,
            private val filter: (QueuedMessage) -> Boolean = {true},
            private val modifier: (QueuedMessage) -> String
    ): Comparable<MessageModifier> {

        /**
         * Adds this modifier to the active modifier set [activeModifiers]
         */
        fun enable() {
            activeModifiers.add(this)
        }

        /**
         * Adds this modifier to the active modifier set [activeModifiers]
         */
        fun disable() {
            activeModifiers.remove(this)
        }

        /**
         * Apple this modifier on [queuedMessage]
         *
         * @param queuedMessage Message to be applied on
         *
         * @return true if [queuedMessage] have been modified
         */
        fun apply(queuedMessage: QueuedMessage) = filter(queuedMessage).also {
            if (it) queuedMessage.packet.packetMessage = modifier(queuedMessage)
        }

        override fun compareTo(other: MessageModifier): Int {
            val result = priority - other.priority
            return if (result != 0) result
            else other.id - id
        }
    }
}