package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by Xiaro on 27/07/20.
 */
@Module.Info(
        name = "Breadcrumbs",
        description = "Draws a tail behind as you move",
        category = Module.Category.RENDER,
        alwaysListening = true
)
class Breadcrumbs : Module() {
    private val clear = register(Settings.b("Clear", false))
    private val whileDisabled = register(Settings.b("WhileDisabled", false))
    private val smooth = register(Settings.floatBuilder("SmoothFactor").withValue(5.0f).withRange(0.0f, 10.0f).build())
    private val maxDistance = register(Settings.integerBuilder("MaxDistance").withValue(4096).withRange(1024, 16384).build())
    private val yOffset = register(Settings.floatBuilder("YOffset").withValue(0.5f).withRange(0.0f, 1.0f).build())
    private val throughBlocks = register(Settings.b("ThroughBlocks", true))
    private val r = register(Settings.integerBuilder("Red").withValue(255).withRange(0, 255).build())
    private val g = register(Settings.integerBuilder("Green").withValue(166).withRange(0, 255).build())
    private val b = register(Settings.integerBuilder("Blue").withValue(188).withRange(0, 255).build())
    private val a = register(Settings.integerBuilder("Alpha").withValue(200).withRange(0, 255).build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).build())

    private val posList = ConcurrentLinkedQueue<Vec3d>()
    private var tickCount = 0

    override fun onToggle() {
        if (!whileDisabled.value) {
            posList.clear()
        }
    }

    override fun onWorldRender(event: RenderEvent?) {
        if (mc.player == null || event == null || (isDisabled && !whileDisabled.value)) {
            posList.clear()
            return
        }

        /* Adding point to list */
        var currentPos = getInterpolatedPos(mc.player, event.partialTicks)
        if (mc.player.isElytraFlying) currentPos = currentPos.subtract(0.0, 0.5, 0.0)
        val minDist = if (isEnabled) (2.01f - smooth.value / 5f) else (2.01f - smooth.value / 5f) * 2f
        if (posList.isEmpty()) {
            posList.add(currentPos)
        } else if (currentPos.distanceTo(posList.last()) > minDist) {
            posList.add(currentPos)
        }

        /* Rendering */
        if (posList.isNotEmpty() && isEnabled) {
            val offset = Vec3d(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY + yOffset.value + 0.05, -mc.renderManager.renderPosZ)
            GL11.glColor4f(r.value / 255f, g.value / 255f, b.value / 255f, a.value / 255f)
            GL11.glLineWidth(thickness.value)
            KamiTessellator.prepareLine(throughBlocks.value)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            for (pos in posList) {
                val renderPos = pos.add(offset)
                GL11.glVertex3d(renderPos.x, renderPos.y, renderPos.z)
            }
            val renderCurrentPos = currentPos.add(offset)
            GL11.glVertex3d(renderCurrentPos.x, renderCurrentPos.y, renderCurrentPos.z)
            GL11.glEnd()
            KamiTessellator.releaseLine(throughBlocks.value)
            GL11.glColor3d(1.0, 1.0, 1.0)
        }
    }

    override fun onUpdate() {
        if (isDisabled && !whileDisabled.value) return

        if (tickCount < 200) {
            tickCount++
        } else {
            val cutoffPos = posList.lastOrNull { pos -> mc.player.getDistance(pos.x, pos.y, pos.z) > maxDistance.value }
            if (cutoffPos != null) while (posList.first() != cutoffPos) {
                posList.remove()
            }
            tickCount = 0
        }
    }

    init {
        clear.settingListener = Setting.SettingListeners {
            if (clear.value) {
                posList.clear()
                sendChatMessage("$chatName Cleared!")
                clear.value = false
            }
            //closeSettings()
        }
    }
}