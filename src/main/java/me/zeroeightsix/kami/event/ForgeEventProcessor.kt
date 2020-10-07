package me.zeroeightsix.kami.event

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.*
import me.zeroeightsix.kami.gui.UIRenderer
import me.zeroeightsix.kami.gui.kami.KamiGUI
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.ProjectionUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.passive.AbstractHorse
import net.minecraftforge.client.event.*
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard

object ForgeEventProcessor {
    private val mc = Wrapper.minecraft
    private var prevWidth = mc.displayWidth
    private var prevHeight = mc.displayHeight

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        KamiMod.EVENT_BUS.post(event)

        if (mc.world != null && mc.player != null) {
            val safeTickEvent = SafeTickEvent(event.phase)
            ModuleManager.onUpdate(safeTickEvent)
        }

        if (event.phase == TickEvent.Phase.END) {
            if (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
                prevWidth = mc.displayWidth
                prevHeight = mc.displayHeight
                KamiMod.EVENT_BUS.post(ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight))
                for (component in KamiMod.getInstance().guiManager.children) {
                    if (component !is Frame) continue
                    KamiGUI.dock(component)
                }
            }
            if (mc.world != null && mc.player != null) {
                KamiMod.getInstance().guiManager.callTick(KamiMod.getInstance().guiManager)
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        ProjectionUtils.updateMatrix()

        mc.profiler.startSection("KamiWorldRender")
        KamiTessellator.prepareGL()
        val renderWorldEvent = RenderWorldEvent(KamiTessellator, event.partialTicks)
        renderWorldEvent.setupTranslation()

        KamiMod.EVENT_BUS.post(renderWorldEvent)
        ModuleManager.onWorldRender(renderWorldEvent)

        KamiTessellator.releaseGL()
        mc.profiler.endSection()
    }

    @SubscribeEvent
    fun onRenderPre(event: RenderGameOverlayEvent.Pre) {
        KamiMod.EVENT_BUS.post(event)
        if (event.isCanceled) return
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        KamiMod.EVENT_BUS.post(event)
        if (event.isCanceled) return

        val target = if (!mc.player.isCreative && mc.player!!.ridingEntity is AbstractHorse) RenderGameOverlayEvent.ElementType.HEALTHMOUNT
        else RenderGameOverlayEvent.ElementType.EXPERIENCE

        if (event.type == target) {
            KamiMod.EVENT_BUS.post(RenderOverlayEvent(event.partialTicks))
            ModuleManager.onRender()
            UIRenderer.renderAndUpdateFrames()
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return
        if (CommandConfig.prefixChat.value && Keyboard.getEventCharacter().toString().equals(Command.getCommandPrefix(), ignoreCase = true) && !mc.player.isSneaking) {
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
    fun onLivingEntityUseItemEventTick(entityUseItemEvent: LivingEntityUseItemEvent.Tick) {
        KamiMod.EVENT_BUS.post(entityUseItemEvent)
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
        KamiMod.EVENT_BUS.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        KamiMod.EVENT_BUS.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    fun onClientConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        KamiMod.EVENT_BUS.post(ConnectionEvent.Connect())
    }
}