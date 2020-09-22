package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.init.MobEffects
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType
import net.minecraftforge.client.event.RenderGameOverlayEvent

@Module.Info(
        name = "AntiOverlay",
        description = "Prevents rendering of fire, water and block texture overlays.",
        category = Module.Category.RENDER
)
object AntiOverlay : Module() {
    private val fire = register(Settings.b("Fire", true))
    private val water = register(Settings.b("Water", true))
    private val blocks = register(Settings.b("Blocks", true))
    private val portals = register(Settings.b("Portals", true))
    private val blindness = register(Settings.b("Blindness", true))
    private val nausea = register(Settings.b("Nausea", true))
    val totems = register(Settings.b("Totems", true))
    private val vignette = register(Settings.b("Vignette", true))
    private val helmet = register(Settings.b("Helmet", true))

    @EventHandler
    private val renderBlockOverlayEventListener = Listener(EventHook { event: RenderBlockOverlayEvent ->
        event.isCanceled = when (event.overlayType) {
            OverlayType.FIRE -> fire.value
            OverlayType.WATER -> water.value
            OverlayType.BLOCK -> blocks.value
            else -> false
        }
    })

    @EventHandler
    private val renderPreGameOverlayEventListener = Listener(EventHook { event: RenderGameOverlayEvent.Pre ->
        event.isCanceled = when (event.type) {
            RenderGameOverlayEvent.ElementType.VIGNETTE -> vignette.value
            RenderGameOverlayEvent.ElementType.PORTAL -> portals.value
            RenderGameOverlayEvent.ElementType.HELMET -> helmet.value
            else -> false
        }
    })

    override fun onUpdate() {
        if (blindness.value) mc.player.removeActivePotionEffect(MobEffects.BLINDNESS)
        if (nausea.value) mc.player.removeActivePotionEffect(MobEffects.NAUSEA)
    }
}