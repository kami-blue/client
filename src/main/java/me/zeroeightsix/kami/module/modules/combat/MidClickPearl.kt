package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.InventoryUtils.swapSlot
import me.zeroeightsix.kami.util.InventoryUtils.swapSlotToItem
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse

@Module.Info(
        name = "MidClickPearl",
        category = Module.Category.COMBAT,
        description = "Throws a pearl automatically when you middle click in air"
)
object MidClickPearl : Module() {
    private var prevSlot = -1
    private var startTime = -1L

    init {
        listener<InputEvent.MouseInputEvent> {
            if (mc.player == null || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit == Type.BLOCK) return@listener

            if (startTime == -1L && Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
                /* 368 is ender pearl */
                if (InventoryUtils.getSlotsHotbar(368) != null) {
                    prevSlot = mc.player.inventory.currentItem
                    swapSlotToItem(368)
                    startTime = 0L
                } else {
                    sendChatMessage("No Ender Pearl was found in hotbar!")
                    startTime = System.currentTimeMillis() + 1000L
                }
            }
        }

        listener<SafeTickEvent> {
            if (startTime == 0L && mc.player.getCooledAttackStrength(0f) >= 1f) {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND)
                startTime = System.currentTimeMillis()
            } else if (startTime > 0L) {
                if (prevSlot != -1) {
                    swapSlot(prevSlot)
                    prevSlot = -1
                }
                if (System.currentTimeMillis() - startTime > 2000L) startTime = -1L
            }
        }
    }
}