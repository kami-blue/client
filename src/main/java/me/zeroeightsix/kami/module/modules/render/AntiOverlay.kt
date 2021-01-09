package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.tutorial.TutorialSteps
import net.minecraft.init.MobEffects
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

object AntiOverlay : Module(
    category = Category.RENDER
) {
    private val fire = setting(getTranslationKey("Fire"), true)
    private val water = setting(getTranslationKey("Water"), true)
    private val blocks = setting(getTranslationKey("Blocks"), true)
    private val portals = setting(getTranslationKey("Portals"), true)
    private val blindness = setting(getTranslationKey("Blindness"), true)
    private val nausea = setting(getTranslationKey("Nausea"), true)
    val totems = setting(getTranslationKey("Totems"), true)
    private val vignette = setting(getTranslationKey("Vignette"), true)
    private val helmet = setting(getTranslationKey("Helmet"), true)
    private val tutorial = setting(getTranslationKey("Tutorial"), true)
    private val potionIcons = setting(getTranslationKey("PotionIcons"), false)

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