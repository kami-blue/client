package org.kamiblue.client.gui.hudgui.elements.misc

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.manager.managers.NetworkManager
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.WebUtils
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.grammar

internal object Queue2B2T : LabelHud(
    name = "2B2T Queue",
    category = Category.MISC,
    description = "Length of 2B2T Queue"
) {
    private const val apiUrl = "https://2bqueue.info/queue"
    private val gson = Gson()
    private val queueData = QueueData(0, 0, 0, 0)

    private val lastUpdateTimer = TickTimer(TimeUnit.SECONDS)
    private val dataUpdateTimer = TickTimer(TimeUnit.SECONDS)

    private val hasShownWarning = setting("Has Shown Warning", false, { false })
    private val show by setting("Show", Show.BOTH)

    private enum class Show {
        BOTH, PRIORITY, REGULAR
    }

    private val showPriority get() = show == Show.BOTH || show == Show.PRIORITY
    private val showRegular get() = show == Show.BOTH || show == Show.REGULAR

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (dataUpdateTimer.tick(15L)) {
                updateQueueData()
            }
        }
    }

    override fun SafeClientEvent.updateText() {
        if (!hasShownWarning.value) {
            MessageSendHelper.sendWarningMessage(
                "This module uses an external API, 2bqueue.info, which is operated by Tycrek at the time of writing." +
                    "If you do not trust this external API / have not verified the safety yourself, disable this HUD component."
            )
            hasShownWarning.value = true
        }

        if (NetworkManager.isOffline) {
            displayText.addLine("Cannot connect to 2bqueue.info", primaryColor)
            displayText.add("Make sure your internet is working!", primaryColor)
        } else {
            if (showPriority) {
                displayText.add("Priority: ", primaryColor)
                displayText.add("${queueData.priority}", secondaryColor)
            }

            if (showRegular) {
                displayText.add("Regular: ", primaryColor)
                displayText.add("${queueData.regular}", secondaryColor)
            }

            displayText.addLine("", primaryColor)
            displayText.add("Last updated ${queueData.getLastUpdate()} ago", primaryColor)
        }
    }

    private fun updateQueueData() {
        val tmpData = try {
            val json = WebUtils.getUrlContents(apiUrl)
            gson.fromJson(json, QueueData::class.java)
        } catch (e: Exception) {
            return
        } ?: return // Gson is not null-safe

        // Instead of overwriting the object, copy the values
        // This is because of the lastUpdateCache
        queueData.priority = tmpData.priority
        queueData.regular = tmpData.regular
        queueData.total = tmpData.total
        queueData.lastUpdated = tmpData.lastUpdated
    }

    private data class QueueData(
        @SerializedName("prio")
        var priority: Int,
        var regular: Int,
        var total: Int,
        @SerializedName("timems")
        var lastUpdated: Long
    ) {
        private var lastUpdateCache: String = "0 minutes, 0 seconds"

        fun getLastUpdate(): String {
            if (lastUpdateTimer.tick(1L)) {
                val difference = System.currentTimeMillis() - lastUpdated

                val minuteAmt = difference / 60000L % 60L
                val secondAmt = difference / 1000L % 60L
                val minutes = grammar(minuteAmt.toInt(), "minute", "minutes")
                val seconds = grammar(secondAmt.toInt(), "second", "seconds")

                lastUpdateCache = "$minutes, $seconds"
            }

            return lastUpdateCache
        }
    }
}