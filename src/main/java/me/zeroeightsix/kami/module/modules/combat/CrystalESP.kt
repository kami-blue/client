package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.ProjectionUtils
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

@Module.Info(
        name = "CrystalESP",
        description = "Renders ESP for End Crystals",
        category = Module.Category.COMBAT
)
object CrystalESP : Module() {
    private val page = register(Settings.e<Page>("Page", Page.DAMAGE_ESP))

    private val damageESP = register(Settings.booleanBuilder("DamageESP").withValue(false).withVisibility { page.value == Page.DAMAGE_ESP })
    private val minAlpha = register(Settings.integerBuilder("MinAlpha").withValue(0).withRange(0, 255).withVisibility { page.value == Page.DAMAGE_ESP })
    private val maxAlpha = register(Settings.integerBuilder("MaxAlpha").withValue(63).withRange(0, 255).withVisibility { page.value == Page.DAMAGE_ESP })
    private val damageRange = register(Settings.floatBuilder("DamageESPRange").withValue(4.0f).withRange(0.0f, 8.0f).withStep(0.5f).withVisibility { page.value == Page.DAMAGE_ESP })

    private val crystalESP = register(Settings.booleanBuilder("CrystalESP").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP })
    private val mode = register(Settings.enumBuilder(Mode::class.java, "Mode").withValue(Mode.BLOCK).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val filled = register(Settings.booleanBuilder("Filled").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val tracer = register(Settings.booleanBuilder("Tracer").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val showDamage = register(Settings.booleanBuilder("Damage").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val showSelfDamage = register(Settings.booleanBuilder("SelfDamage").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val textScale = register(Settings.floatBuilder("TextScale").withValue(1.0f).withRange(0.0f, 4.0f).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val animationScale = register(Settings.floatBuilder("AnimationScale").withValue(1.0f).withRange(0.0f, 2.0f).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val crystalRange = register(Settings.floatBuilder("CrystalESPRange").withValue(16.0f).withRange(0.0f, 64.0f).withVisibility { page.value == Page.CRYSTAL_ESP })

    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value })
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value })
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value })
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(47).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && filled.value })
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && outline.value })
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && tracer.value })
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(2.0f).withRange(0.0f, 4.0f).withVisibility { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && (outline.value || tracer.value) })

    private enum class Page {
        DAMAGE_ESP, CRYSTAL_ESP, CRYSTAL_ESP_COLOR
    }

    private enum class Mode {
        BLOCK, CRYSTAL
    }

    private val crystalMap = ConcurrentHashMap<EntityEnderCrystal, Pair<Pair<Float, Float>, Vec2f>>() // <Crystal, <<Damage, SelfDamage>, <PrevAlpha, Alpha>>>
    private val runnable = Runnable { updateCrystalESP() }
    private val executor = Executors.newSingleThreadExecutor()

    init {
        listener<SafeTickEvent> {
            if (it.phase == TickEvent.Phase.END) executor.execute(runnable)
        }
    }

    private fun updateCrystalESP() {
        if (crystalESP.value) {
            val cacheMap = HashMap(CrystalUtils.getCrystalList(crystalRange.value).associateWith { crystal ->
                val damages = calcDamages(crystal)
                damages to Vec2f(0f, 0f)
            })

            for ((crystal, pair) in crystalMap) {
                val scale = 1f / animationScale.value
                val damages = calcDamages(crystal)
                cacheMap.computeIfPresent(crystal) { _, _ -> Pair(damages, Vec2f(pair.second.y, min(pair.second.y + 0.4f * scale, 1f))) }
                cacheMap.computeIfAbsent(crystal) { Pair(damages, Vec2f(pair.second.y, min(pair.second.y + 0.2f * scale, 2f))) }
            }
            crystalMap.putAll(cacheMap)
            crystalMap.values.removeIf { it.second.y >= 2.0f }
        } else {
            crystalMap.clear()
        }
    }

    private fun calcDamages(crystal: EntityEnderCrystal): Pair<Float, Float> {
        val damage = CombatManager.target?.let { CrystalUtils.calcDamage(crystal, it) } ?: -0.0f
        val selfDamage = mc.player?.let { CrystalUtils.calcDamage(crystal, mc.player) } ?: -0.0f
        return Pair(damage, selfDamage)
    }
    
    init {
        listener<RenderWorldEvent> {
            val renderer = ESPRenderer()

            /* Damage ESP */
            val placeList = CombatManager.crystalPlaceList
            if (damageESP.value && placeList.isNotEmpty()) {
                renderer.aFilled = 255
                for ((pos, damage, _) in placeList) {
                    val rgb = MathUtils.convertRange(damage.toInt(), 0, 20, 127, 255)
                    val a = MathUtils.convertRange(damage.toInt(), 0, 20, minAlpha.value, maxAlpha.value)
                    val rgba = ColorHolder(rgb, rgb, rgb, a)
                    renderer.add(pos, rgba)
                }
                renderer.render(true)
            }

            /* Crystal ESP */
            if (crystalESP.value) {
                renderer.aFilled = if (filled.value) aFilled.value else 0
                renderer.aOutline = if (outline.value) aOutline.value else 0
                renderer.aTracer = if (tracer.value) aTracer.value else 0
                renderer.thickness = thickness.value
                for ((crystal, pair) in crystalMap) {
                    val progress = getAnimationProgress(pair.second)
                    val box = if (mode.value == Mode.CRYSTAL) {
                        crystal.boundingBox.shrink(1.0 - progress)
                    } else {
                        AxisAlignedBB(crystal.position.down()).shrink(0.5 - progress * 0.5)
                    }
                    val rgba = ColorHolder(r.value, g.value, b.value, (progress * 255f).toInt())
                    renderer.add(box, rgba)
                }
                renderer.render(true)
            }
        }
        
        listener<RenderOverlayEvent> {
            if (!showDamage.value && !showSelfDamage.value) return@listener
            GlStateUtils.rescale(mc.displayWidth.toDouble(), mc.displayHeight.toDouble())
            for ((crystal, pair) in crystalMap) {
                glPushMatrix()
                val screenPos = ProjectionUtils.toScreenPos(if (mode.value == Mode.CRYSTAL) {
                    crystal.boundingBox.center
                } else {
                    crystal.positionVector.subtract(0.0, 0.5, 0.0)
                })
                glTranslated(screenPos.x, screenPos.y, 0.0)
                glScalef(textScale.value * 2f, textScale.value * 2f, 1f)

                val damage = abs(MathUtils.round(pair.first.first, 1))
                val selfDamage = abs(MathUtils.round(pair.first.second, 1))
                val alpha = (getAnimationProgress(pair.second) * 255f).toInt()
                val color = ColorHolder(255, 255, 255, alpha)
                if (showDamage.value) {
                    val text = "Target: $damage"
                    val halfWidth = FontRenderAdapter.getStringWidth(text) / -2f
                    FontRenderAdapter.drawString(text, halfWidth, 0f, color = color)
                }
                if (showSelfDamage.value) {
                    val text = "Self: $selfDamage"
                    val halfWidth = FontRenderAdapter.getStringWidth(text) / -2f
                    FontRenderAdapter.drawString(text, halfWidth, FontRenderAdapter.getFontHeight() + 2f, color = color)
                }

                glPopMatrix()
            }
            GlStateUtils.rescaleMc()
        }
    }

    private fun getAnimationProgress(progressIn: Vec2f): Float {
        val interpolated = progressIn.x + (progressIn.y - progressIn.x) * KamiTessellator.pTicks()
        return sin(interpolated * 0.5 * PI).toFloat()
    }
}