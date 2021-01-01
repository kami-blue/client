package me.zeroeightsix.kami.event

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.ResolutionUpdateEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.kami.KamiGUI
import me.zeroeightsix.kami.gui.mc.KamiGuiChat
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.ProjectionUtils
import me.zeroeightsix.kami.util.text.MessageDetection
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
                for (component in KamiMod.INSTANCE.guiManager.children) {
                    if (component !is Frame) continue
                    KamiGUI.dock(component)
                }
            }
            if (mc.world != null && mc.player != null) {
                KamiMod.INSTANCE.guiManager.callTick(KamiMod.INSTANCE.guiManager)
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
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        KamiEventBus.post(event)
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return
        if (!mc.player.isSneaking) {
            val prefix = CommandManager.prefix
            val typedChar = Keyboard.getEventCharacter().toString()
            if (prefix.length == 1 && typedChar.equals(CommandManager.prefix, true)) {
                mc.displayGuiScreen(KamiGuiChat(CommandManager.prefix))
            }
        }

        KamiEventBus.post(event)
        ModuleManager.onBind(Keyboard.getEventKey())
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatSent(event: ClientChatEvent) {
        if (MessageDetection.Command.KAMI_BLUE detect event.message) {
            CommandManager.runCommand(event.message.removePrefix(CommandManager.prefix))
            event.isCanceled = true
        }
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