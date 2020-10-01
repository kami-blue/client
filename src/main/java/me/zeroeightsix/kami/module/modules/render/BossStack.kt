package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import net.minecraft.client.gui.BossInfoClient
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import org.lwjgl.opengl.GL11.glColor4f
import org.lwjgl.opengl.GL11.glScalef
import kotlin.math.abs
import kotlin.math.roundToInt

@Module.Info(
        name = "BossStack",
        description = "Modify the boss health GUI to take up less space",
        category = Module.Category.RENDER
)
object BossStack : Module() {
    private val mode = register(Settings.e<BossStackMode>("Mode", BossStackMode.STACK))
    private val scale = register(Settings.floatBuilder("Scale").withValue(1.0f).withRange(0.1f, 5.0f))

    private enum class BossStackMode {
        REMOVE, MINIMIZE, STACK
    }

    private val texture = ResourceLocation("textures/gui/bars.png")
    private val bossInfoMap = LinkedHashMap<BossInfoClient, Int>()

    override fun onUpdate() {
        bossInfoMap.clear()
        val bossInfoList = mc.ingameGUI.bossOverlay.mapBossInfos?.values ?: return
        when (mode.value as BossStackMode) {
            BossStackMode.REMOVE -> {
            }
            BossStackMode.MINIMIZE -> {
                val closest = bossInfoList.minBy { findMatchBoss(it)?.getDistance(mc.player) ?: Float.MAX_VALUE }
                        ?: return
                bossInfoMap[closest] = -1
            }
            BossStackMode.STACK -> {
                val cacheMap = HashMap<String, ArrayList<BossInfoClient>>()
                for (bossInfo in bossInfoList) {
                    val list = cacheMap.getOrDefault(bossInfo.name.formattedText, ArrayList())
                    cacheMap.putIfAbsent(bossInfo.name.formattedText, list)
                    list.add(bossInfo)
                }
                for (list in cacheMap.values) {
                    val closest = list.minBy { findMatchBoss(it)?.getDistance(mc.player) ?: Float.MAX_VALUE }
                            ?: continue
                    bossInfoMap[closest] = list.size
                }
            }
        }
    }

    private fun findMatchBoss(bossInfo: BossInfoClient): EntityLivingBase? {
        val bossHealthMap = HashMap<EntityLivingBase, Float>()
        for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityLivingBase) continue
            if (entity.isNonBoss) continue
            if (entity.displayName.formattedText != bossInfo.name.formattedText) continue
            bossHealthMap[entity] = (entity.health / entity.maxHealth) * 100.0f
        }
        if (bossHealthMap.isEmpty()) return null
        var closestDiff = 100.0f
        for (percent in bossHealthMap.values) {
            val diff = abs(percent - bossInfo.percent)
            if (diff > closestDiff) continue
            closestDiff = diff
        }
        bossHealthMap.values.removeIf { abs(it - bossInfo.percent) > closestDiff }
        return bossHealthMap.keys.minBy { mc.player.getDistance(it) }
    }

    @EventHandler
    private val listener = Listener(EventHook { event: RenderGameOverlayEvent.Pre ->
        if (event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH) return@EventHook
        event.isCanceled = true

        mc.profiler.startSection("bossHealth")
        val width = ScaledResolution(mc).scaledWidth
        var posY = 12
        GlStateUtils.blend(true)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        if (bossInfoMap.isNotEmpty()) for ((bossInfo, count) in bossInfoMap) {
            val posX = (width / scale.value / 2.0f - 91f).roundToInt()
            val text = bossInfo.name.formattedText + if (count != -1) " x$count" else ""
            val textPosX = width / scale.value / 2.0f - mc.fontRenderer.getStringWidth(text) / 2.0f
            val textPosY = posY - 9.0f

            glScalef(scale.value, scale.value, 1.0f)
            mc.textureManager.bindTexture(texture)
            mc.ingameGUI.bossOverlay.render(posX, posY, bossInfo)
            mc.fontRenderer.drawStringWithShadow(text, textPosX, textPosY, 0xffffff)
            glScalef(1.0f / scale.value, 1.0f / scale.value, 1.0f)

            posY += 10 + mc.fontRenderer.FONT_HEIGHT
        }
        mc.profiler.endSection()
    })
}