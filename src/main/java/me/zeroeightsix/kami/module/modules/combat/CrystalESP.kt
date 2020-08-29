package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CombatUtils.CrystalUtils
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.BlockPos
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.HashMap
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

class CrystalESP : Module() {
    private val damageESP = register(Settings.booleanBuilder("DamageESP").withValue(true).build())
    private val minAlpha = register(Settings.integerBuilder("MinAlpha").withValue(15).withRange(0, 255).build())
    private val maxAlpha = register(Settings.integerBuilder("MaxAlpha").withValue(63).withRange(0, 255).build())
    private val crystalESP = register(Settings.booleanBuilder("CrystalESP").withValue(true).build())
    private val filled = register(Settings.booleanBuilder("Filled").withValue(true).withVisibility { crystalESP.value }.build())
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { crystalESP.value }.build())
    private val tracer = register(Settings.booleanBuilder("Tracer").withValue(true).withVisibility { crystalESP.value }.build())
    private val animationScale = register(Settings.floatBuilder("AnimationScale").withValue(1.0f).withRange(0.0f, 5.0f).withVisibility { crystalESP.value })
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { crystalESP.value }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { crystalESP.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { crystalESP.value }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(47).withRange(0, 255).withVisibility { crystalESP.value && filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { crystalESP.value && outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { crystalESP.value && tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(4.0f).withRange(0.0f, 8.0f).withVisibility { crystalESP.value }.build())
    private val espRange = register(Settings.floatBuilder("ESPRange").withValue(16.0f).withRange(0.0f, 32.0f).build())

    private val damageESPMap = TreeMap<Float, BlockPos>(Comparator.reverseOrder())
    private val crystalList = HashMap<EntityEnderCrystal, Float>()

    override fun onUpdate() {
        damageESPMap.clear()
        if (damageESP.value) {
            damageESPMap.putAll(CrystalUtils.getPlacePos(CombatManager.target, CombatManager.target, espRange.value.toDouble()))
        }

        if (crystalESP.value) {
            val cacheList = HashMap(CrystalUtils.getCrystalList(espRange.value))
            for ((crystal, alpha) in crystalList) {
                if (alpha >= 2.0f) {
                    crystalList.remove(crystal)
                } else {
                    val scale = 1f / animationScale.value
                    cacheList.computeIfPresent(crystal) { _, _ -> min(alpha + 0.1f * scale, 1f) }
                    cacheList.computeIfAbsent(crystal) { min(alpha + 0.05f * scale, 2f) }
                }
            }
            crystalList.putAll(cacheList)
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
            for ((crystal, alpha) in crystalList) {
                val sine = sin(alpha * 0.5 * PI).toFloat()
                val box = crystal.boundingBox.shrink(1.0 - sine)
                val rgba = ColorHolder(r.value, g.value, b.value, (sine * 255f).toInt())
                renderer.add(box, rgba)
            }
            renderer.render(true)
        }
    }
}