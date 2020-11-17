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
        KamiEventBus.post(event)

        if (mc.world != null && mc.player != null) {
            SafeTickEvent(event.phase).also {
                KamiEventBus.post(it)
            }
        }

        if (event.phase == TickEvent.Phase.END) {
            if (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
                prevWidth = mc.displayWidth
                prevHeight = mc.displayHeight
                KamiEventBus.post(ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight))
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

        KamiEventBus.post(renderWorldEvent)

        KamiTessellator.releaseGL()
        mc.profiler.endSection()
    }

    @SubscribeEvent
    fun onRenderPre(event: RenderGameOverlayEvent.Pre) {
        KamiEventBus.post(event)
        if (event.isCanceled) return
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        KamiEventBus.post(event)
        if (event.isCanceled) return

        val target = if (!mc.player.isCreative && mc.player!!.ridingEntity is AbstractHorse) RenderGameOverlayEvent.ElementType.HEALTHMOUNT
        else RenderGameOverlayEvent.ElementType.EXPERIENCE

        if (event.type == target) {
            KamiEventBus.post(RenderOverlayEvent(event.partialTicks))
            UIRenderer.renderAndUpdateFrames()
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return
        if (CommandConfig.prefixChat.value && Keyboard.getEventCharacter().toString().equals(Command.getCommandPrefix(), ignoreCase = true) && !mc.player.isSneaking) {
            mc.displayGuiScreen(GuiChat(Command.getCommandPrefix()))
        } else {
            KamiEventBus.post(event)
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
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onEventMouse(event: InputEvent.MouseInputEvent) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onChunkLoaded(event: ChunkEvent.Unload) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onInputUpdate(event: InputUpdateEvent) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onLivingEntityUseItemEventTick(entityUseItemEvent: LivingEntityUseItemEvent.Tick) {
        KamiEventBus.post(entityUseItemEvent)
    }

    @SubscribeEvent
    fun onPlayerPush(event: PlayerSPPushOutOfBlocksEvent) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onLeftClickBlock(event: PlayerInteractEvent.LeftClickBlock) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onAttackEntity(entityEvent: AttackEntityEvent) {
        KamiEventBus.post(entityEvent)
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    fun onClientChat(event: ClientChatReceivedEvent) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onServerDisconnect(event: FMLNetworkEvent.ServerDisconnectionFromClientEvent) {
        KamiEventBus.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        KamiEventBus.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onClientConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        KamiEventBus.post(ConnectionEvent.Connect())
    }
}