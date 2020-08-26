package me.zeroeightsix.kami.event

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.commands.PeekCommand
import me.zeroeightsix.kami.event.events.DisplaySizeChangedEvent
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent
import me.zeroeightsix.kami.gui.UIRenderer
import me.zeroeightsix.kami.gui.kami.KamiGUI
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.module.modules.render.AntiOverlay
import me.zeroeightsix.kami.module.modules.render.BossStack
import me.zeroeightsix.kami.module.modules.render.HungerOverlay
import me.zeroeightsix.kami.module.modules.render.NoRender
import me.zeroeightsix.kami.util.HungerOverlayRenderHelper
import me.zeroeightsix.kami.util.HungerOverlayUtils
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.*
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard

/**
 * Created by 086 on 11/11/2017.
 * Updated by Qther on 18/02/20
 * Updated by dominikaaaa on 18/02/20
 * Updated by Xiaro on 04/08/20
 *
 * TODO: Run the HungerOverlay coeds in its own class instead of here
 */
open class ForgeEventProcessor {
    private val mc = Wrapper.minecraft
    private var displayWidth = 0
    private var displayHeight = 0
    private var flashAlpha = 0f
    private var alphaDir: Byte = 1
    private var foodIconsOffset = 0
    private val hungerOverlay: HungerOverlay get() = ModuleManager.getModuleT(HungerOverlay::class.java)!!

    @SubscribeEvent
    fun onUpdate(event: LivingEvent.LivingUpdateEvent) {
        if (mc.world != null && event.entity.entityWorld.isRemote && event.entityLiving == mc.player) {
            val localPlayerUpdateEvent: Event = LocalPlayerUpdateEvent(event.entityLiving)
            KamiMod.EVENT_BUS.post(localPlayerUpdateEvent)
            event.isCanceled = localPlayerUpdateEvent.isCanceled
        }

        if (event.isCanceled) return

        if (mc.displayWidth != displayWidth || mc.displayHeight != displayHeight) {
            KamiMod.EVENT_BUS.post(DisplaySizeChangedEvent())
            displayWidth = mc.displayWidth
            displayHeight = mc.displayHeight
            for (component in KamiMod.getInstance().guiManager.children) {
                if (component !is Frame) continue
                KamiGUI.dock(component)
            }
        }

        if (PeekCommand.sb != null) {
            val scaledResolution = ScaledResolution(mc)
            val scaledWidth = scaledResolution.scaledWidth
            val scaledHeight = scaledResolution.scaledHeight
            val gui = GuiShulkerBox(mc.player!!.inventory, PeekCommand.sb)
            gui.setWorldAndResolution(mc, scaledWidth, scaledHeight)
            mc.displayGuiScreen(gui)
            PeekCommand.sb = null
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.world == null || mc.player == null) return

        if (ModuleManager.isModuleEnabled(NoRender::class.java) && ModuleManager.getModuleT(NoRender::class.java)!!.items.value && event.phase == TickEvent.Phase.START) {
            for (potentialItem in mc.world.getLoadedEntityList()) {
                (potentialItem as? EntityItem)?.setDead()
            }
        }

        if (hungerOverlay.isEnabled) {
            if (event.phase != TickEvent.Phase.END) {
                return
            }
            flashAlpha += alphaDir * 0.125f
            if (flashAlpha >= 1.5f) {
                flashAlpha = 1f
                alphaDir = -1
            } else if (flashAlpha <= -0.5f) {
                flashAlpha = 0f
                alphaDir = 1
            }
        }

        GuiIngameForge.renderPortal = !ModuleManager.isModuleEnabled(AntiOverlay::class.java) || !ModuleManager.getModuleT(AntiOverlay::class.java)!!.portals.value
        ModuleManager.onUpdate()
        KamiMod.getInstance().guiManager.callTick(KamiMod.getInstance().guiManager)
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (event.isCanceled) return
        ModuleManager.onWorldRender(event)
    }

    @SubscribeEvent
    fun onRenderPre(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.BOSSINFO && ModuleManager.isModuleEnabled(BossStack::class.java)) {
            event.isCanceled = true
        }
        if (event.isCanceled) return

        if (hungerOverlay.isEnabled) {
            if (event.type != RenderGameOverlayEvent.ElementType.FOOD) {
                return
            }
            if (!hungerOverlay.foodExhaustionOverlay.value) {
                return
            }
            foodIconsOffset = GuiIngameForge.right_height
            val scale = event.resolution
            val left = scale.scaledWidth / 2 + 91
            val top = scale.scaledHeight - foodIconsOffset
            HungerOverlayRenderHelper.drawExhaustionOverlay(HungerOverlayUtils.getExhaustion(mc.player), mc, left, top, 1f)
        }

    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        if (event.isCanceled) return
        var target = RenderGameOverlayEvent.ElementType.EXPERIENCE
        if (!mc.player.isCreative && mc.player!!.ridingEntity is AbstractHorse) {
            target = RenderGameOverlayEvent.ElementType.HEALTHMOUNT
        }

        if (event.type == target) {
            ModuleManager.onRender()
            GlStateManager.pushMatrix()
            UIRenderer.renderAndUpdateFrames()
            GlStateManager.popMatrix()
        } else if (event.type == RenderGameOverlayEvent.ElementType.BOSSINFO && ModuleManager.isModuleEnabled(BossStack::class.java)) {
            BossStack.render(event)
        }

        if (hungerOverlay.isEnabled) {
            if (event.type != RenderGameOverlayEvent.ElementType.FOOD) {
                return
            }
            if (!hungerOverlay.foodValueOverlay.value && !hungerOverlay.saturationOverlay.value) {
                return
            }
            val mc = mc
            val player: EntityPlayer = mc.player
            val heldItem = player.heldItemMainhand
            val stats = player.getFoodStats()
            val scale = event.resolution
            val left = scale.scaledWidth / 2 + 91
            val top = scale.scaledHeight - foodIconsOffset
            if (hungerOverlay.saturationOverlay.value) {
                HungerOverlayRenderHelper.drawSaturationOverlay(0f, stats.saturationLevel, mc, left, top, 1f)
            }
            if (!hungerOverlay.foodValueOverlay.value || heldItem.isEmpty() || !HungerOverlayUtils.isFood(heldItem)) {
                flashAlpha = 0f
                alphaDir = 1
                return
            }
            val foodValues = HungerOverlayUtils.getDefaultFoodValues(heldItem)
            HungerOverlayRenderHelper.drawHungerOverlay(foodValues.hunger, stats.foodLevel, mc, left, top, flashAlpha)
            if (hungerOverlay.saturationOverlay.value) {
                val newFoodValue = stats.foodLevel + foodValues.hunger
                val newSaturationValue = stats.saturationLevel + foodValues.saturationIncrement
                HungerOverlayRenderHelper.drawSaturationOverlay(if (newSaturationValue > newFoodValue) newFoodValue - stats.saturationLevel else foodValues.saturationIncrement, stats.saturationLevel, mc, left, top, flashAlpha)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return
        val commandConfig = ModuleManager.getModuleT(CommandConfig::class.java)
        if (commandConfig!!.prefixChat.value && ("" + Keyboard.getEventCharacter()).equals(Command.getCommandPrefix(), ignoreCase = true) && !mc.player.isSneaking) {
            mc.displayGuiScreen(GuiChat(Command.getCommandPrefix()))
        } else {
            KamiMod.EVENT_BUS.post(event)
            ModuleManager.onBind(Keyboard.getEventKey())
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatSent(event: ClientChatEvent) {
        if (!event.message.startsWith(Command.getCommandPrefix())) return
        event.isCanceled = true
        try {
            mc.ingameGUI.chatGUI.addToSentMessages(event.message)
            if (event.message.length > 1) KamiMod.getInstance().commandManager.callCommand(event.message.substring(Command.getCommandPrefix().length - 1))
            else MessageSendHelper.sendChatMessage("Please enter a command!")
        } catch (e: Exception) {
            e.printStackTrace()
            MessageSendHelper.sendChatMessage("Error occurred while running command! (" + e.message + "), check the log for info!")
        }
        event.message = ""
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerDrawn(event: RenderPlayerEvent.Pre) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerDrawn(event: RenderPlayerEvent.Post) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onChunkLoaded(event: ChunkEvent.Load) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onEventMouse(event: InputEvent.MouseInputEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onChunkLoaded(event: ChunkEvent.Unload) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onInputUpdate(event: InputUpdateEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onLivingEntityUseItemEventTick(entityUseItemEvent: LivingEntityUseItemEvent.Start) {
        KamiMod.EVENT_BUS.post(entityUseItemEvent)
    }

    @SubscribeEvent
    fun onLivingDamageEvent(event: LivingDamageEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onEntityJoinWorldEvent(entityJoinWorldEvent: EntityJoinWorldEvent) {
        KamiMod.EVENT_BUS.post(entityJoinWorldEvent)
    }

    @SubscribeEvent
    fun onPlayerPush(event: PlayerSPPushOutOfBlocksEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onLeftClickBlock(event: PlayerInteractEvent.LeftClickBlock) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onAttackEntity(entityEvent: AttackEntityEvent) {
        KamiMod.EVENT_BUS.post(entityEvent)
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onClientChat(event: ClientChatReceivedEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onServerDisconnect(event: FMLNetworkEvent.ServerDisconnectionFromClientEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    @SubscribeEvent
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        KamiMod.EVENT_BUS.post(event)
    }

    companion object {
        @JvmField
        val icons = ResourceLocation(KamiMod.MODID, "textures/hungeroverlay.png")
    }
}