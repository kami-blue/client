package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getNameFromUUID
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.MathUtils.round
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityTameable
import java.util.*
import kotlin.math.pow

@Module.Info(
        name = "MobOwner",
        description = "Displays the owner of tamed mobs",
        category = Module.Category.RENDER)

object MobOwner : Module() {
    private val speed = register(Settings.b("Speed", true))
    private val jump = register(Settings.b("Jump", true))
    private val hp = register(Settings.b("Health", true))
    private val requestTime = register(Settings.integerBuilder("CacheReset").withValue(20).withRange(10, 200).withStep(10))
    private val debug = register(Settings.b("Debug", true))

    private var startTime = 0L /* Periodically try to re-request invalid UUIDs */
    private var startTime1 = 0L /* Super safe method to limit requests to the Mojang API in case you load more then 10 different UUIDs */
    private val cachedUUIDs = HashMap<String, String>() // <UUID, Username>
    private var apiRequests = 0
    private const val invalidText = "Offline or invalid UUID!"

    init {
        listener<SafeTickEvent> {
            resetRequests()
            resetCache()
            for (entity in mc.world.loadedEntityList) {
                /* Non Horse types, such as wolves */
                if (entity is EntityTameable) {
                    if (entity.isTamed && entity.owner != null) {
                        entity.alwaysRenderNameTag = true
                        entity.customNameTag = "Owner: " + entity.owner!!.displayName.formattedText + getHealth(entity)
                    }
                }
                if (entity is AbstractHorse) {
                    if (!entity.isTame || entity.ownerUniqueId == null) {
                        continue
                    }
                    entity.alwaysRenderNameTag = true
                    entity.customNameTag = "Owner: " + getUsername(entity.ownerUniqueId.toString()) + getSpeed(entity) + getJump(entity) + getHealth(entity)
                }
            }
        }
    }

    override fun onDisable() {
        cachedUUIDs.clear()
        for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityTameable) {
                if (entity !is AbstractHorse) {
                    continue
                }
            }
            try {
                entity.alwaysRenderNameTag = false
            } catch (ignored: Exception) {
            }
        }
    }

    private fun getUsername(uuid: String): String {
        for ((key, value) in cachedUUIDs) {
            if (key.equals(uuid, ignoreCase = true)) {
                return value
            }
        }
        try {
            if (apiRequests > 10) {
                return "Too many API requests"
            }
            cachedUUIDs[uuid] = getNameFromUUID(uuid)?.replace("\"", "") ?: invalidText
            apiRequests++
        } catch (illegal: IllegalStateException) { /* this means the json parsing failed meaning the UUID is invalid */
            cachedUUIDs[uuid] = invalidText
        }
        /* Run this again to reduce the amount of requests made to the Mojang API */for ((key, value) in cachedUUIDs) {
            if (key.equals(uuid, ignoreCase = true)) {
                return value
            }
        }
        return invalidText
    }

    private fun resetCache() {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + requestTime.value * 1000 <= System.currentTimeMillis()) { // 1 requestTime = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            for ((key) in cachedUUIDs) {
                if (key.equals(invalidText, ignoreCase = true)) {
                    cachedUUIDs.clear()
                    if (debug.value) sendChatMessage("$chatName Reset cached UUIDs list!")
                    return
                }
            }
        }
    }

    private fun resetRequests() {
        if (startTime1 == 0L) startTime1 = System.currentTimeMillis()
        if (startTime1 + 10 * 1000 <= System.currentTimeMillis()) { // 10 seconds
            startTime1 = System.currentTimeMillis()
            if (apiRequests >= 2) {
                apiRequests = 0
                if (debug.value) sendChatMessage("$chatName Reset API requests counter!")
            }
        }
    }

    private fun getSpeed(horse: AbstractHorse): String {
        return if (!speed.value) "" else " S: " + round(43.17 * horse.aiMoveSpeed, 2)
    }

    private fun getJump(horse: AbstractHorse): String {
        return if (!jump.value) "" else " J: " + round(-0.1817584952 * horse.horseJumpStrength.pow(3.0) + 3.689713992 * horse.horseJumpStrength.pow(2.0) + 2.128599134 * horse.horseJumpStrength - 0.343930367, 2)
    }

    private fun getHealth(horse: AbstractHorse): String {
        return if (!hp.value) "" else " HP: " + round(horse.health, 2)
    }

    private fun getHealth(tameable: EntityTameable): String {
        return if (!hp.value) "" else " HP: " + round(tameable.health, 2)
    }
}