package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.tutorial.TutorialSteps
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
    private val fire = setting("Fire", true)
    private val water = setting("Water", true)
    private val blocks = setting("Blocks", true)
    private val portals = setting("Portals", true)
    private val blindness = setting("Blindness", true)
    private val nausea = setting("Nausea", true)
    val totems = setting("Totems", true)
    private val vignette = setting("Vignette", true)
    private val helmet = setting("Helmet", true)
    private val tutorial = setting("Tutorial", true)

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

        listener<SafeTickEvent> {
            if (blindness.value) mc.player.removeActivePotionEffect(MobEffects.BLINDNESS)
            if (nausea.value) mc.player.removeActivePotionEffect(MobEffects.NAUSEA)
            if (tutorial.value) mc.gameSettings.tutorialStep = TutorialSteps.NONE
        }
    }
}