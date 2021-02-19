package org.kamiblue.client.module.modules.misc

import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemNameTag
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener

internal object AutoNametag : Module(
    name = "AutoNametag",
    description = "Automatically nametags entities",
    category = Category.MISC
) {
    private val modeSetting = setting("Mode", Mode.ANY)
    private val range = setting("Range", 3.5f, 2.0f..8.0f, 0.5f)
    private val debug = setting("Debug", false)

    private enum class Mode {
        WITHER, ANY
    }

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
        for (entity in world.loadedEntityList) {
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
}