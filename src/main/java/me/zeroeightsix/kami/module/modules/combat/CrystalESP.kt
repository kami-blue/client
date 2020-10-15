package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Quad
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.ProjectionUtils
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11.*
import kotlin.math.*

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
    private val crystalRange = register(Settings.floatBuilder("CrystalESPRange").withValue(16.0f).withRange(0.0f, 16.0f).withVisibility { page.value == Page.CRYSTAL_ESP })

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

    private var placeList = emptyList<Triple<BlockPos, Float, Float>>()
    private val crystalMap = LinkedHashMap<EntityEnderCrystal, Quad<Float, Float, Float, Float>>() // <Crystal, <Target Damage, Self Damage, Prev Progress, Progress>>

    init {
        listener<SafeTickEvent> {
            val eyePos = mc.player.getPositionEyes(1.0f)

            placeList = if (damageESP.value) {
                val squaredRange = damageRange.value.pow(2)
                CombatManager.crystalPlaceList.filter { it.first.distanceSqToCenter(eyePos.x, eyePos.y, eyePos.z) <= squaredRange }
            } else {
                emptyList()
            }

            if (crystalESP.value) {
                val cacheMap = CombatManager.crystalMap.entries
                        .filter { it.key.positionVector.distanceTo(eyePos) < crystalRange.value }
                        .associate { it.key to Quad(it.value.first, it.value.second, 0.0f, 0.0f) }.toMutableMap()
                val scale = 1.0f / animationScale.value

                for ((crystal, quad1) in crystalMap) {
                    cacheMap.computeIfPresent(crystal) { _, quad2 -> Quad(quad2.first, quad2.second, quad1.fourth, min(quad1.fourth + 0.4f * scale, 1.0f)) }
                    if (quad1.fourth < 2.0f) cacheMap.computeIfAbsent(crystal) { Quad(quad1.first, quad1.second, quad1.fourth, min(quad1.fourth + 0.2f * scale, 2.0f)) }
                }

                crystalMap.clear()
                crystalMap.putAll(cacheMap)
            } else {
                crystalMap.clear()
            }
        }

        listener<RenderWorldEvent> {
            val renderer = ESPRenderer()

            /* Damage ESP */
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
            if (crystalESP.value && crystalMap.isNotEmpty()) {
                renderer.aFilled = if (filled.value) aFilled.value else 0
                renderer.aOutline = if (outline.value) aOutline.value else 0
                renderer.aTracer = if (tracer.value) aTracer.value else 0
                renderer.thickness = thickness.value

                for ((crystal, quad) in crystalMap) {
                    val progress = getAnimationProgress(quad.third, quad.fourth)
                    val box = if (mode.value == Mode.CRYSTAL) crystal.boundingBox.shrink(1.0 - progress)
                    else AxisAlignedBB(crystal.position.down()).shrink(0.5 - progress * 0.5)
                    val rgba = ColorHolder(r.value, g.value, b.value, (progress * 255.0f).toInt())
                    renderer.add(box, rgba)
                }

                renderer.render(true)
            }
        }

        listener<RenderOverlayEvent> {
            if (!showDamage.value && !showSelfDamage.value) return@listener
            GlStateUtils.rescale(mc.displayWidth.toDouble(), mc.displayHeight.toDouble())

            for ((crystal, quad) in crystalMap) {
                glPushMatrix()

                val screenPos = ProjectionUtils.toScreenPos(
                        if (mode.value == Mode.CRYSTAL) crystal.boundingBox.center
                        else crystal.positionVector.subtract(0.0, 0.5, 0.0)
                )
                glTranslated(screenPos.x, screenPos.y, 0.0)
                glScalef(textScale.value * 2.0f, textScale.value * 2.0f, 1.0f)

                val damage = abs(MathUtils.round(quad.first, 1))
                val selfDamage = abs(MathUtils.round(quad.second, 1))
                val alpha = (getAnimationProgress(quad.third, quad.fourth) * 255f).toInt()
                val color = ColorHolder(255, 255, 255, alpha)

                if (showDamage.value) {
                    val text = "Target: $damage"
                    val halfWidth = FontRenderAdapter.getStringWidth(text) / -2.0f
                    FontRenderAdapter.drawString(text, halfWidth, 0f, color = color)
                }
                if (showSelfDamage.value) {
                    val text = "Self: $selfDamage"
                    val halfWidth = FontRenderAdapter.getStringWidth(text) / -2.0f
                    FontRenderAdapter.drawString(text, halfWidth, FontRenderAdapter.getFontHeight() + 2.0f, color = color)
                }

                glPopMatrix()
            }

            GlStateUtils.rescaleMc()
        }
    }

    private fun getAnimationProgress(prevProgress: Float, progress: Float): Float {
        val interpolated = prevProgress + (progress - prevProgress) * KamiTessellator.pTicks()
        return sin(interpolated * 0.5 * PI).toFloat()
    }
}