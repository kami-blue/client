package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CombatUtils.CrystalUtils
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

@Module.Info(
        name = "CrystalESP",
        description = "Renders ESP for End Crystals",
        category = Module.Category.COMBAT
)
class CrystalESP : Module() {
    private val page = register(Settings.e<Page>("Page", Page.DAMAGE_ESP))

    private val damageESP = register(Settings.booleanBuilder("DamageESP").withValue(true).withVisibility { page.value == Page.DAMAGE_ESP }.build())
    private val minAlpha = register(Settings.integerBuilder("MinAlpha").withValue(15).withRange(0, 255).withVisibility { page.value == Page.DAMAGE_ESP }.build())
    private val maxAlpha = register(Settings.integerBuilder("MaxAlpha").withValue(63).withRange(0, 255).withVisibility { page.value == Page.DAMAGE_ESP }.build())
    private val damageRange = register(Settings.floatBuilder("DamageESPRange").withValue(4.0f).withRange(0.0f, 16.0f).withVisibility { page.value == Page.DAMAGE_ESP }.build())

    private val crystalESP = register(Settings.booleanBuilder("CrystalESP").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP }.build())
    private val filled = register(Settings.booleanBuilder("Filled").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val tracer = register(Settings.booleanBuilder("Tracer").withValue(true).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val animationScale = register(Settings.floatBuilder("AnimationScale").withValue(1.0f).withRange(0.0f, 4.0f).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(47).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value && filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value && outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value && tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(4.0f).withRange(0.0f, 8.0f).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value }.build())
    private val crystalRange = register(Settings.floatBuilder("CrystalESPRange").withValue(16.0f).withRange(0.0f, 48.0f).withVisibility { page.value == Page.CRYSTAL_ESP }.build())

    private enum class Page {
        DAMAGE_ESP, CRYSTAL_ESP
    }

    private val damageESPMap = ConcurrentHashMap<Float, BlockPos>()
    private val crystalMap = ConcurrentHashMap<EntityEnderCrystal, Vec2f>() // <Crystal, <PrevAlpha, Alpha>>
    private val threads = arrayOf(Thread { updateDamageESP() }, Thread { updateCrystalESP() })
    private val threadPool = Executors.newCachedThreadPool()

    override fun onUpdate() {
        for (thread in threads) {
            threadPool.execute(thread)
        }
    }

    private fun updateDamageESP() {
        if (damageESP.value) {
            val cacheMap = CrystalUtils.getPlacePos(CombatManager.target, CombatManager.target, damageRange.value)
            damageESPMap.values.removeIf { !cacheMap.containsValue(it) }
            damageESPMap.putAll(cacheMap)
        } else {
            damageESPMap.clear()
        }
    }

    private fun updateCrystalESP() {
        if (crystalESP.value) {
            val cacheMap = HashMap(CrystalUtils.getCrystalList(crystalRange.value).map { it to Vec2f(0f, 0f) }.toMap())

            crystalMap.values.removeIf { it.x >= 2.0f }
            for ((crystal, pair) in crystalMap) {
                val scale = 1f / animationScale.value
                cacheMap.computeIfPresent(crystal) { _, _ -> Vec2f(pair.y, min(pair.y + 0.4f * scale, 1f)) }
                cacheMap.computeIfAbsent(crystal) { Vec2f(pair.y, min(pair.y + 0.2f * scale, 2f)) }
            }
            crystalMap.putAll(cacheMap)
        } else {
            crystalMap.clear()
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        val renderer = ESPRenderer()

        /* Damage ESP */
        if (damageESP.value && damageESPMap.isNotEmpty()) {
            renderer.aFilled = 255
            for ((damage, pos) in damageESPMap) {
                val rgb = MathUtils.convertRange(damage.toInt(), 16, 64, 127, 255)
                val a = MathUtils.convertRange(damage.toInt(), 16, 64, minAlpha.value, maxAlpha.value)
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
            for ((crystal, alpha) in crystalMap) {
                val interpolatedAlpha = alpha.x + (alpha.y - alpha.x) * KamiTessellator.pTicks()
                val sine = sin(interpolatedAlpha * 0.5 * PI).toFloat()
                val box = crystal.boundingBox.shrink(1.0 - sine)
                val rgba = ColorHolder(r.value, g.value, b.value, (sine * 255f).toInt())
                renderer.add(box, rgba)
            }
            renderer.render(true)
        }
    }
}