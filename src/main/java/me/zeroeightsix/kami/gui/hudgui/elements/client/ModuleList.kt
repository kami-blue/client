package me.zeroeightsix.kami.gui.hudgui.elements.client

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.TimedFlag
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.graphics.font.HAlign
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.graphics.font.VAlign
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.sumByFloat
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

object ModuleList : HudElement(
    name = "ModuleList",
    category = Category.CLIENT,
    description = "List of enabled modules"
) {

    private val sortingMode = setting("SortingMode", SortingMode.LENGTH)
    private val showInvisible = setting("ShowInvisible", false)
    private val rainbow = setting("Rainbow", true)
    private val rainbowLength = setting("RainbowLength", 10.0f, 1.0f..20.0f, 0.5f, { rainbow.value })
    private val indexedHue = setting("IndexedHue", 0.5f, 0.0f..1.0f, 0.05f)
    private val primary = setting("PrimaryColor", ColorHolder(155, 144, 255), false)
    private val secondary = setting("SecondaryColor", ColorHolder(255, 255, 255), false)

    @Suppress("UNUSED")
    private enum class SortingMode(
        override val displayName: String,
        val comparator: Comparator<Module>
    ) : DisplayEnum {
        LENGTH("Length", compareByDescending { it.textLine.getWidth() }),
        ALPHABET("Alphabet", compareBy { it.name }),
        CATEGORY("Category", compareBy { it.category.ordinal })
    }

    override val maxWidth: Float
        get() = sortedModuleList.firstOrNull { toggleMap[it]?.value == true }?.let {
            max(it.textLine.getWidth(), 100.0f)
        } ?: 80.0f

    override val maxHeight: Float
        get() = max(toggleMap.values.sumByFloat { it.displayHeight }, 160.0f)

    private val sortedModuleList = LinkedList(ModuleManager.getModules())
    private val textLineMap = HashMap<Module, TextComponent.TextLine>()
    private val toggleMap = ModuleManager.getModules()
        .associateWith { TimedFlag(false) }
        .toMutableMap()

    init {
        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END || !visible.value) return@listener

            for ((module, timedFlag) in toggleMap) {
                val state = module.isEnabled && (module.isVisible || showInvisible.value)
                if (timedFlag.value != state) timedFlag.value = state

                if (timedFlag.progress <= 0.0f) continue
                textLineMap[module] = module.newTextLine
            }

            sortedModuleList.sortWith(sortingMode.value.comparator)
        }
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        GlStateManager.pushMatrix()

        GlStateManager.translate(renderWidth * dockingH.value.multiplier, 0.0f, 0.0f)
        if (dockingV.value == VAlign.BOTTOM) GlStateManager.translate(0.0f, renderHeight, 0.0f)

        drawModuleList()

        GlStateManager.popMatrix()
    }

    private fun drawModuleList() {
        val primaryHsb = Color.RGBtoHSB(primary.value.r, primary.value.g, primary.value.b, null)
        val lengthMs = rainbowLength.value * 1000.0f
        val timedHue = System.currentTimeMillis() % lengthMs.toLong() / lengthMs

        for ((index, module) in sortedModuleList.withIndex()) {
            val timedFlag = toggleMap[module] ?: continue
            val progress = timedFlag.progress

            if (progress <= 0.0f) continue

            GlStateManager.pushMatrix()

            val textLine = module.textLine
            val textWidth = textLine.getWidth()
            val animationXOffset = textWidth * (dockingH.value.multiplier * 2.0f - 1.0f) * (1.0f - progress)
            val stringPosX = textWidth * dockingH.value.multiplier

            GlStateManager.translate(animationXOffset - stringPosX, 0.0f, 0.0f)

            if (rainbow.value) {
                val hue = timedHue + indexedHue.value * 0.005f * index
                val color = ColorConverter.hexToRgb(Color.HSBtoRGB(hue, primaryHsb[1], primaryHsb[2]))

                TextComponent.TextLine(" ").run {
                    add(TextComponent.TextElement(module.name, color))
                    module.getHudInfo().let {
                        if (it.isNotBlank()) add(TextComponent.TextElement(it, secondary.value))
                    }
                    if (dockingH.value == HAlign.RIGHT) reverse()
                    drawLine(progress, true, HAlign.LEFT, FontRenderAdapter.useCustomFont)
                }
            } else {
                textLine.drawLine(progress, true, HAlign.LEFT, FontRenderAdapter.useCustomFont)
            }

            GlStateManager.popMatrix()
            var yOffset = timedFlag.displayHeight
            if (dockingV.value == VAlign.BOTTOM) yOffset *= -1.0f
            GlStateManager.translate(0.0f, yOffset, 0.0f)
        }
    }

    private val Module.textLine
        get() = textLineMap.getOrPut(this) {
            this.newTextLine
        }

    private val Module.newTextLine
        get() = TextComponent.TextLine(" ").apply {
            add(TextComponent.TextElement(name, primary.value))
            getHudInfo().let {
                if (it.isNotBlank()) add(TextComponent.TextElement(it, secondary.value))
            }
            if (dockingH.value == HAlign.RIGHT) reverse()
        }

    private val TimedFlag<Boolean>.displayHeight
        get() = (FontRenderAdapter.getFontHeight() + 2.0f) * progress

    private val TimedFlag<Boolean>.progress
        get() = if (value) {
            AnimationUtils.exponentInc(AnimationUtils.toDeltaTimeFloat(lastUpdateTime), 200.0f)
        } else {
            AnimationUtils.exponentDec(AnimationUtils.toDeltaTimeFloat(lastUpdateTime), 200.0f)
        }

}