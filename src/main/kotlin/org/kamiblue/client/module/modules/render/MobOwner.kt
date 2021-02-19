package org.kamiblue.client.module.modules.render

import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityTameable
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.UUIDManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.MathUtils.round
import kotlin.math.pow

internal object MobOwner : Module(
    name = "MobOwner",
    description = "Displays the owner of tamed mobs",
    category = Category.RENDER
) {
    private val speed = setting("Speed", true)
    private val jump = setting("Jump", true)
    private val hp = setting("Health", true)

    private const val invalidText = "Offline or invalid UUID!"

    init {
        safeListener<TickEvent.ClientTickEvent> {
            for (entity in world.loadedEntityList) {
                /* Non Horse types, such as wolves */
                if (entity is EntityTameable) {
                    val owner = entity.owner
                    if (!entity.isTamed || owner == null) continue

                    entity.alwaysRenderNameTag = true
                    entity.customNameTag = "Owner: " + owner.displayName.formattedText + getHealth(entity)
                }

                if (entity is AbstractHorse) {
                    val ownerUUID = entity.ownerUniqueId
                    if (!entity.isTame || ownerUUID == null) continue

                    val ownerName = UUIDManager.getByUUID(ownerUUID)?.name ?: invalidText
                    entity.alwaysRenderNameTag = true
                    entity.customNameTag = "Owner: " + ownerName + getSpeed(entity) + getJump(entity) + getHealth(entity)
                }
            }
        }

        onDisable {
            runSafe {
                for (entity in world.loadedEntityList) {
                    if (entity !is AbstractHorse) continue

                    try {
                        entity.alwaysRenderNameTag = false
                    } catch (_: Exception) {
                        // Ignored
                    }
                }
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