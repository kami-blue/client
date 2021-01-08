package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.manager.managers.UUIDManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityTameable
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.round
import kotlin.math.pow

object MobOwner : Module(
    name = KamiLang.get("module.modules.render.MobOwner.Mobowner"),
    description = KamiLang.get("module.modules.render.MobOwner.DisplaysTheOwnerOf"),
    category = Category.RENDER
) {
    private val speed = setting(KamiLang.get("module.modules.render.MobOwner.Speed"), true)
    private val jump = setting(KamiLang.get("module.modules.render.MobOwner.Jump"), true)
    private val hp = setting(KamiLang.get("module.modules.render.MobOwner.Health"), true)

    private val invalidText = KamiLang.get("module.modules.render.MobOwner.OfflineOrInvalidUuid!")

    init {
        safeListener<TickEvent.ClientTickEvent> {
            for (entity in world.loadedEntityList) {
                /* Non Horse types, such as wolves */
                if (entity is EntityTameable) {
                    val owner = entity.owner
                    if (!entity.isTamed || owner == null) continue

                    entity.alwaysRenderNameTag = true
                    entity.customNameTag = KamiLang.get("module.modules.render.MobOwner.Owner:") + owner.displayName.formattedText + getHealth(entity)
                }

                if (entity is AbstractHorse) {
                    val ownerUUID = entity.ownerUniqueId
                    if (!entity.isTame || ownerUUID == null) continue

                    val ownerName = UUIDManager.getByUUID(ownerUUID)?.name ?: invalidText
                    entity.alwaysRenderNameTag = true
                    entity.customNameTag = KamiLang.get("module.modules.render.MobOwner.Owner:") + ownerName + getSpeed(entity) + getJump(entity) + getHealth(entity)
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
        return if (!speed.value) "" else KamiLang.get("module.modules.render.MobOwner.speedChar") + round(43.17 * horse.aiMoveSpeed, 2)
    }

    private fun getJump(horse: AbstractHorse): String {
        return if (!jump.value) "" else KamiLang.get("module.modules.render.MobOwner.jumpChar") + round(-0.1817584952 * horse.horseJumpStrength.pow(3.0) + 3.689713992 * horse.horseJumpStrength.pow(2.0) + 2.128599134 * horse.horseJumpStrength - 0.343930367, 2)
    }

    private fun getHealth(horse: AbstractHorse): String {
        return if (!hp.value) "" else KamiLang.get("module.modules.render.MobOwner.HitpointsChar") + round(horse.health, 2)
    }

    private fun getHealth(tameable: EntityTameable): String {
        return if (!hp.value) "" else KamiLang.get("module.modules.render.MobOwner.HitpointsChar") + round(tameable.health, 2)
    }
}