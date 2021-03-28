package org.kamiblue.client.module.modules.render

import net.minecraft.client.tutorial.TutorialSteps
import net.minecraft.init.MobEffects
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener

internal object AntiOverlay : Module(
    name = "AntiOverlay",
    description = "Prevents rendering of fire, water and block texture overlays.",
    category = Category.RENDER
) {
    val hurtCamera = setting("Hurt Camera", true)
    private val fire = setting("Fire", true)
    private val water = setting("Water", true)
    private val blocks = setting("Blocks", true)
    private val portals = setting("Portals", true)
    private val blindness = setting("Blindness", true)
    private val nausea = setting("Nausea", true)
    val totems = setting("Totems", true)
    private val vignette = setting("Vignette", false)
    private val helmet = setting("Helmet", true)
    private val tutorial = setting("Tutorial", true)
    private val potionIcons = setting("Potion Icons", false)

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