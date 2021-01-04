package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemNameTag
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
    name = "AutoNametag",
    description = "Automatically nametags entities",
    category = Module.Category.MISC
)
object AutoNametag : Module() {
    private val modeSetting = register(Settings.e<Mode>("Mode", Mode.ANY))
    private val range = register(Settings.floatBuilder("Range").withValue(3.5f).withRange(2.0f, 8.0f).withStep(0.5f))
    private val debug = register(Settings.b("Debug", false))

    private var currentName = ""
    private var currentSlot = -1

    init {
        safeListener<TickEvent.ClientTickEvent> {
            findNameTags()
            useNameTag()
        }
    }

    private fun SafeClientEvent.useNameTag() {
        val originalSlot = player.inventory.currentItem
        loop@ for (entity in world.loadedEntityList) {
            when (modeSetting.value) {
                Mode.WITHER -> {
                    if (entity is EntityWither
                        && entity.displayName.unformattedText != currentName
                        && player.getDistance(entity) <= range.value
                    ) {
                        nametagEntity(entity)
                    }
                }
                Mode.ANY -> {
                    if ((entity is EntityMob || entity is EntityAnimal)
                        && entity.displayName.unformattedText != currentName
                        && player.getDistance(entity) <= range.value
                    ) {
                        nametagEntity(entity)
                    }
                }
                else -> continue@loop
            }
        }
        player.inventory.currentItem = originalSlot
    }

    private fun SafeClientEvent.nametagEntity(entity: Entity) {
        if (debug.value) MessageSendHelper.sendChatMessage("Found unnamed " + entity.displayName.unformattedText)

        if (currentSlot == -1 || !isNametag(currentSlot)) {
            MessageSendHelper.sendErrorMessage("$chatName Error: No nametags in hotbar")
            disable()
            return
        }

        player.inventory.currentItem = currentSlot
        playerController.interactWithEntity(player, entity, EnumHand.MAIN_HAND)
    }

    private fun SafeClientEvent.findNameTags() {
        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item is ItemBlock) continue

            if (isNametag(i)) {
                currentName = stack.displayName
                currentSlot = i
            }
        }
    }

    /* In case they run out of nametags, check again */
    private fun SafeClientEvent.isNametag(i: Int): Boolean {
        val stack = player.inventory.getStackInSlot(i)
        val tag = stack.item
        return tag is ItemNameTag && stack.displayName != "Name Tag"
    }

    private enum class Mode {
        WITHER, ANY
    }
}