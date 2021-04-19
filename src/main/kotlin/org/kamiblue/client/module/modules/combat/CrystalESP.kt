package org.kamiblue.client.module.modules.combat

import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.HotbarManager.serverSideItem
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.Quad
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.combat.CrystalUtils.canPlaceCollide
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.KamiTessellator
import org.kamiblue.client.util.graphics.ProjectionUtils
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

internal object CrystalESP : Module(
    name = "CrystalESP",
    description = "Renders ESP for End Crystals",
    category = Category.COMBAT
) {
    private val page = setting("Page", Page.DAMAGE_ESP)

    private val damageESP = setting("Damage ESP", false, { page.value == Page.DAMAGE_ESP })
    private val minAlpha = setting("Min Alpha", 0, 0..255, 1, { page.value == Page.DAMAGE_ESP })
    private val maxAlpha = setting("Max Alpha", 63, 0..255, 1, { page.value == Page.DAMAGE_ESP })
    private val damageRange = setting("Damage ESP Range", 4.0f, 0.0f..8.0f, 0.5f, { page.value == Page.DAMAGE_ESP })

    private val crystalESP = setting("Crystal ESP", true, { page.value == Page.CRYSTAL_ESP })
    private val onlyOwn = setting("Only Own", false, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val filled = setting("Filled", true, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val outline = setting("Outline", true, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val tracer = setting("Tracer", true, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val showDamage = setting("Damage", true, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val showSelfDamage = setting("Self Damage", true, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val textScale = setting("Text Scale", 1.0f, 0.0f..4.0f, 0.25f, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val animationScale = setting("Animation Scale", 1.0f, 0.0f..2.0f, 0.1f, { page.value == Page.CRYSTAL_ESP && crystalESP.value })
    private val crystalRange = setting("Crystal ESP Range", 16.0f, 0.0f..16.0f, 0.5f, { page.value == Page.CRYSTAL_ESP })

    private val color by setting("Color", ColorHolder(155, 144, 255), false, { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value })
    private val aFilled = setting("Filled Alpha", 47, 0..255, 1, { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && filled.value })
    private val aOutline = setting("Outline Alpha", 127, 0..255, 1, { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && outline.value })
    private val aTracer = setting("Tracer Alpha", 200, 0..255, 1, { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && tracer.value })
    private val thickness = setting("Thickness", 2.0f, 0.25f..4.0f, 0.25f, { page.value == Page.CRYSTAL_ESP_COLOR && crystalESP.value && (outline.value || tracer.value) })

    private enum class Page {
        DAMAGE_ESP, CRYSTAL_ESP, CRYSTAL_ESP_COLOR
    }

    private var placeMap = emptyMap<BlockPos, CombatManager.CrystalDamage>()
    private val renderCrystalMap = LinkedHashMap<BlockPos, Quad<Float, Float, Float, Float>>() // <Crystal, <Target Damage, Self Damage, Prev Progress, Progress>>
    private val pendingPlacing = LinkedHashMap<BlockPos, Long>()

    init {
        safeListener<PacketEvent.PostSend>(0) {
            if (it.packet !is CPacketPlayerTryUseItemOnBlock) return@safeListener

            if (checkHeldItem(it.packet) && canPlaceCollide(it.packet.pos)) {
                pendingPlacing[it.packet.pos] = System.currentTimeMillis()
            }
        }
    }

    private fun SafeClientEvent.checkHeldItem(packet: CPacketPlayerTryUseItemOnBlock) = packet.hand == EnumHand.MAIN_HAND
        && player.serverSideItem.item == Items.END_CRYSTAL
        || packet.hand == EnumHand.OFF_HAND
        && player.heldItemOffhand.item == Items.END_CRYSTAL

    init {
        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeListener
            updateDamageESP()
            updateCrystalESP()
        }
    }

    private fun updateDamageESP() {
        placeMap = if (damageESP.value) {
            CombatManager.placeMap.filter { it.value.distance <= damageRange.value }
        } else {
            emptyMap()
        }
    }

    private fun updateCrystalESP() {
        if (crystalESP.value) {
            val placeMap = CombatManager.placeMap
            val crystalMap = CombatManager.crystalMap
            val cacheMap = HashMap<BlockPos, Quad<Float, Float, Float, Float>>()

            // Removes after 1 second
            pendingPlacing.entries.removeIf { System.currentTimeMillis() - it.value > 1000L }

            if (!onlyOwn.value) {
                for ((crystal, calculation) in crystalMap) {
                    if (calculation.distance > crystalRange.value) continue
                    cacheMap[crystal.position.down()] = Quad(calculation.targetDamage, calculation.selfDamage, 0.0f, 0.0f)
                }
            }

            for (pos in pendingPlacing.keys) {
                val damage = placeMap[pos] ?: continue
                cacheMap[pos] = Quad(damage.targetDamage, damage.selfDamage, 0.0f, 0.0f)
            }

            val scale = 1.0f / animationScale.value
            for ((pos, quad1) in renderCrystalMap) {
                cacheMap.computeIfPresent(pos) { _, quad2 -> Quad(quad2.first, quad2.second, quad1.fourth, min(quad1.fourth + 0.4f * scale, 1.0f)) }
                if (quad1.fourth < 2.0f) cacheMap.computeIfAbsent(pos) { Quad(quad1.first, quad1.second, quad1.fourth, min(quad1.fourth + 0.2f * scale, 2.0f)) }
            }

            renderCrystalMap.clear()
            renderCrystalMap.putAll(cacheMap)
        } else {
            renderCrystalMap.clear()
        }
    }

    init {
        listener<RenderWorldEvent> {
            val renderer = ESPRenderer()

            /* Damage ESP */
            if (damageESP.value && placeMap.isNotEmpty()) {
                renderer.aFilled = 255

                for ((pos, calculation) in placeMap) {
                    val rgb = MathUtils.convertRange(calculation.targetDamage.toInt(), 0, 20, 127, 255)
                    val a = MathUtils.convertRange(calculation.targetDamage.toInt(), 0, 20, minAlpha.value, maxAlpha.value)
                    val rgba = ColorHolder(rgb, rgb, rgb, a)
                    renderer.add(pos, rgba)
                }

                renderer.render(true)
            }

            /* Crystal ESP */
            if (crystalESP.value && renderCrystalMap.isNotEmpty()) {
                renderer.aFilled = if (filled.value) aFilled.value else 0
                renderer.aOutline = if (outline.value) aOutline.value else 0
                renderer.aTracer = if (tracer.value) aTracer.value else 0
                renderer.thickness = thickness.value

                for ((pos, quad) in renderCrystalMap) {
                    val progress = getAnimationProgress(quad.third, quad.fourth)
                    val box = AxisAlignedBB(pos).shrink(0.5 - progress * 0.5)
                    color.a = (progress * 255.0f).toInt()
                    renderer.add(box, color)
                }

                renderer.render(true)
            }
        }

        listener<RenderOverlayEvent> {
            if (!showDamage.value && !showSelfDamage.value) return@listener
            GlStateUtils.rescaleActual()

            for ((pos, quad) in renderCrystalMap) {
                glPushMatrix()

                val screenPos = ProjectionUtils.toScreenPos(pos.toVec3dCenter())
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