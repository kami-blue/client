package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
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

    init {
        listener<RenderBlockOverlayEvent> {
            it.isCanceled = when (it.overlayType) {
                OverlayType.FIRE -> fire.value
                OverlayType.WATER -> water.value
                OverlayType.BLOCK -> blocks.value
                else -> it.isCanceled
            }
        }

        listener<RenderGameOverlayEvent.Pre> {
            it.isCanceled = when (it.type) {
                RenderGameOverlayEvent.ElementType.VIGNETTE -> vignette.value
                RenderGameOverlayEvent.ElementType.PORTAL -> portals.value
                RenderGameOverlayEvent.ElementType.HELMET -> helmet.value
                else -> it.isCanceled
            }
        }
    }

    override fun onUpdate(event: SafeTickEvent) {
        if (blindness.value) mc.player.removeActivePotionEffect(MobEffects.BLINDNESS)
        if (nausea.value) mc.player.removeActivePotionEffect(MobEffects.NAUSEA)
    }
}