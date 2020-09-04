package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.potion.Potion
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType
import net.minecraftforge.client.event.RenderGameOverlayEvent

/**
 * Created by Dewy on the 20th April, 2020
 */
@Module.Info(
        name = "AntiOverlay",
        description = "Prevents rendering of fire, water and block texture overlays.",
        category = Module.Category.RENDER
)
class AntiOverlay : Module() {
    private val fire = register(Settings.b("Fire",true))
    private val water = register(Settings.b("Water",true))
    private val blocks = register(Settings.b("Blocks",true))
    private val portals = register(Settings.b("Portals",true))
    private val blindness = register(Settings.b("Blindness",true))
    private val nausea = register(Settings.b("Nausea",true))
    @JvmField
    val totems = register(Settings.b("Totems",true))
    private val vignette = register(Settings.b("Vignette",true))
    private val helmet = register(Settings.b("Helmet",true))

    @EventHandler
    var renderBlockOverlayEventListener = Listener(EventHook { event: RenderBlockOverlayEvent ->
        var shouldCancel = false
        if (!isEnabled) {
            return@EventHook
        }
        when (event.overlayType) {
            OverlayType.FIRE -> shouldCancel = fire.value
            OverlayType.WATER -> shouldCancel = water.value
            OverlayType.BLOCK -> shouldCancel = blocks.value
        }
        event.isCanceled = shouldCancel
    })

    @EventHandler
    var renderPreGameOverlayEventListener = Listener(EventHook { event: RenderGameOverlayEvent.Pre ->
        var shouldCancel = false
        if (!isEnabled) {
            return@EventHook
        }
        when (event.type) {
            RenderGameOverlayEvent.ElementType.VIGNETTE -> shouldCancel = vignette.value
            RenderGameOverlayEvent.ElementType.PORTAL -> shouldCancel = portals.value
            RenderGameOverlayEvent.ElementType.HELMET -> shouldCancel = helmet.value
        }
        event.isCanceled = shouldCancel
    })

    override fun onUpdate() {
        if (blindness.value) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("blindness"))
        }
        if (nausea.value) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("nausea"))
        }
    }
}