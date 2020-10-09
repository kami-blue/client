package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemNameTag
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

@Module.Info(
        name = "AutoNametag",
        description = "Automatically nametags entities",
        category = Module.Category.MISC
)
object AutoNametag : Module() {
    private val modeSetting = register(Settings.e<Mode>("Mode", Mode.ANY))
    private val range = register(Settings.floatBuilder("Range").withMinimum(2.0f).withValue(3.5f).withMaximum(10.0f).build())
    private val debug = register(Settings.b("Debug", false))

    private var currentName = ""
    private var currentSlot = -1

    override fun onUpdate(event: SafeTickEvent) {
        findNameTags()
        useNameTag()
    }

    private fun useNameTag() {
        val originalSlot = mc.player.inventory.currentItem
        for (w in mc.world.getLoadedEntityList()) {
            when (modeSetting.value) {
                Mode.WITHER -> if (w is EntityWither && w.getDisplayName().unformattedText != currentName) {
                    if (mc.player.getDistance(w) <= range.value) {
                        if (debug.value) MessageSendHelper.sendChatMessage("Found unnamed " + w.getDisplayName().unformattedText)
                        selectNameTags()
                        mc.playerController.interactWithEntity(mc.player, w, EnumHand.MAIN_HAND)
                    }
                }
                Mode.ANY -> if ((w is EntityMob || w is EntityAnimal) && w.displayName.unformattedText != currentName) {
                    if (mc.player.getDistance(w) <= range.value) {
                        if (debug.value) MessageSendHelper.sendChatMessage("Found unnamed " + w.displayName.unformattedText)
                        selectNameTags()
                        mc.playerController.interactWithEntity(mc.player, w, EnumHand.MAIN_HAND)
                    }
                }
            }
        }
        mc.player.inventory.currentItem = originalSlot
    }

    private fun selectNameTags() {
        if (currentSlot == -1 || !isNametag(currentSlot)) {
            MessageSendHelper.sendErrorMessage("$chatName Error: No nametags in hotbar")
            disable()
            return
        }
        mc.player.inventory.currentItem = currentSlot
    }

    private fun findNameTags() {
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.getItem() is ItemBlock) continue

            if (isNametag(i)) {
                currentName = stack.displayName
                currentSlot = i
            }
        }
    }

    /* In case they run out of nametags, check again */
    private fun isNametag(i: Int): Boolean {
        val stack = mc.player.inventory.getStackInSlot(i)
        val tag = stack.getItem()
        return tag is ItemNameTag && stack.displayName != "Name Tag"
    }

    private enum class Mode {
        WITHER, ANY
    }
}