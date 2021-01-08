package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
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
    name = KamiLang.get("module.modules.render.AntiOverlay.Antioverlay"),
    description = KamiLang.get("module.modules.render.AntiOverlay.PreventsRenderingOfFire,"),
    category = Category.RENDER
) {
    private val fire = setting(KamiLang.get("module.modules.render.AntiOverlay.Fire"), true)
    private val water = setting(KamiLang.get("module.modules.render.AntiOverlay.Water"), true)
    private val blocks = setting(KamiLang.get("module.modules.render.AntiOverlay.Blocks"), true)
    private val portals = setting(KamiLang.get("module.modules.render.AntiOverlay.Portals"), true)
    private val blindness = setting(KamiLang.get("module.modules.render.AntiOverlay.Blindness"), true)
    private val nausea = setting(KamiLang.get("module.modules.render.AntiOverlay.Nausea"), true)
    val totems = setting(KamiLang.get("module.modules.render.AntiOverlay.Totems"), true)
    private val vignette = setting(KamiLang.get("module.modules.render.AntiOverlay.Vignette"), true)
    private val helmet = setting(KamiLang.get("module.modules.render.AntiOverlay.Helmet"), true)
    private val tutorial = setting(KamiLang.get("module.modules.render.AntiOverlay.Tutorial"), true)
    private val potionIcons = setting(KamiLang.get("module.modules.render.AntiOverlay.Potionicons"), false)

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