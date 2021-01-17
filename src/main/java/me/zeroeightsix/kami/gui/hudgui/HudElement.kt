package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.rgui.windows.BasicWindow
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.module.modules.client.Hud
import me.zeroeightsix.kami.setting.GuiConfig
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.setting.Translatable
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.threads.safeListener
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.interfaces.DisplayEnum
import org.lwjgl.opengl.GL11.glScalef

open class HudElement(
    val alias: Array<String> = emptyArray(),
    val category: Category,
    val alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false
) : BasicWindow(name = TranslationKeyBlank(),20.0f, 20.0f, 100.0f, 50.0f, SettingGroup.HUD_GUI) {

    val scale by setting("gui.hudgui.HudElement.Scale", 1.0f, 0.1f..4.0f, 0.05f)

    val description: TranslationKey = getTranslationKey("HudElementDescription")

    override val resizable = false

    final override val minWidth: Float get() = FontRenderAdapter.getFontHeight() * scale * 2.0f
    final override val minHeight: Float get() = FontRenderAdapter.getFontHeight() * scale

    final override val maxWidth: Float get() = hudWidth * scale
    final override val maxHeight: Float get() = hudHeight * scale

    open val hudWidth: Float get() = 20f
    open val hudHeight: Float get() = 10f
    //TODO look at this, I don't quite know what this should be.
    val settingList get() = GuiConfig.getGroupOrPut(SettingGroup.HUD_GUI.groupName).getGroupOrPut(originalName.defaultValue).getSettings()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END || !visible) return@safeListener
            width = maxWidth
            height = maxHeight
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        if (alwaysListening || visible) KamiEventBus.subscribe(this)
    }

    override fun onClosed() {
        super.onClosed()
        if (alwaysListening || visible) KamiEventBus.subscribe(this)
    }

    final override fun onTick() {
        super.onTick()
    }

    final override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        renderFrame(vertexHelper)
        glScalef(scale, scale, scale)
        renderHud(vertexHelper)
    }

    open fun renderHud(vertexHelper: VertexHelper) {}

    open fun renderFrame(vertexHelper: VertexHelper) {
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2f(renderWidth, renderHeight).toVec2d(), GuiColors.backGround)
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2f(renderWidth, renderHeight).toVec2d(), 1.5f, GuiColors.outline)
    }

    init {
        visibleSetting.valueListeners.add { _, it ->
            if (it) {
                KamiEventBus.subscribe(this)
                lastActiveTime = System.currentTimeMillis()
            } else if (!alwaysListening) {
                KamiEventBus.unsubscribe(this)
            }
        }

        if (!enabledByDefault) visible = false
    }

    enum class Category(val displayName: TranslationKey){
        CLIENT(TranslationKey("HudElement.Category.Client")),
        COMBAT(TranslationKey("HudElement.Category.Combat")),
        PLAYER(TranslationKey("HudElement.Category.Player")),
        WORLD(TranslationKey("HudElement.Category.World")),
        MISC(TranslationKey("HudElement.Category.Misc"))
    }

    protected companion object {
        val primaryColor get() = Hud.primaryColor
        val secondaryColor get() = Hud.secondaryColor
    }

}