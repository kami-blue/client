package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.tutorial.TutorialSteps
import net.minecraft.init.MobEffects
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

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
    private val tutorial = register(Settings.b("Tutorial", true))
    val potionIcons = register(Settings.b("PotionIcons", false))

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
                RenderGameOverlayEvent.ElementType.POTION_ICONS -> potionIcons.value
                else -> it.isCanceled
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (blindness.value) player.removeActivePotionEffect(MobEffects.BLINDNESS)
            if (nausea.value) player.removeActivePotionEffect(MobEffects.NAUSEA)
            if (tutorial.value) mc.gameSettings.tutorialStep = TutorialSteps.NONE
        }
    }
}