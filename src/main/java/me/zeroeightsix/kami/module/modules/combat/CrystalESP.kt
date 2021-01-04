package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Quad
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.ProjectionUtils
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3dCenter
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
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
    private val onlyOwn = register(Settings.booleanBuilder("OnlyOwn").withValue(false).withVisibility { page.value == Page.CRYSTAL_ESP && crystalESP.value })
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

    private var placeMap = emptyMap<BlockPos, Triple<Float, Float, Double>>()
    private val renderCrystalMap = LinkedHashMap<BlockPos, Quad<Float, Float, Float, Float>>() // <Crystal, <Target Damage, Self Damage, Prev Progress, Progress>>
    private val pendingPlacing = LinkedHashMap<BlockPos, Long>()

    init {
        listener<PacketEvent.PostSend>(0) {
            if (mc.player == null || it.packet !is CPacketPlayerTryUseItemOnBlock) return@listener
            if (checkHeldItem(it.packet) && CrystalUtils.canPlaceCollide(it.packet.pos)) {
                pendingPlacing[it.packet.pos] = System.currentTimeMillis()
            }
        }
    }

    private fun checkHeldItem(packet: CPacketPlayerTryUseItemOnBlock) = packet.hand == EnumHand.MAIN_HAND
            && mc.player.inventory.getStackInSlot(PlayerPacketManager.serverSideHotbar).getItem() == Items.END_CRYSTAL
            || packet.hand == EnumHand.OFF_HAND
            && mc.player.heldItemOffhand.getItem() == Items.END_CRYSTAL

    init {
        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeListener
            updateDamageESP()
            updateCrystalESP()
        }
    }

    private fun updateDamageESP() {
        placeMap = if (damageESP.value) {
            CombatManager.placeMap.filter { it.value.third <= damageRange.value }
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
                for ((crystal, triple) in crystalMap) {
                    if (triple.third > crystalRange.value) continue
                    cacheMap[crystal.position.down()] = Quad(triple.first, triple.second, 0.0f, 0.0f)
                }
            }

            for (pos in pendingPlacing.keys) {
                val damage = placeMap[pos]?: continue
                cacheMap[pos] = Quad(damage.first, damage.second, 0.0f, 0.0f)
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

                for ((pos, triple) in placeMap) {
                    val rgb = MathUtils.convertRange(triple.first.toInt(), 0, 20, 127, 255)
                    val a = MathUtils.convertRange(triple.first.toInt(), 0, 20, minAlpha.value, maxAlpha.value)
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
                    val color = ColorHolder(r.value, g.value, b.value, (progress * 255.0f).toInt())
                    renderer.add(box, color)
                }

                renderer.render(true)
            }
        }

        listener<RenderOverlayEvent> {
            if (!showDamage.value && !showSelfDamage.value) return@listener
            GlStateUtils.rescale(mc.displayWidth.toDouble(), mc.displayHeight.toDouble())

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