package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.EntityUtils.mobTypeSettings
import me.zeroeightsix.kami.util.color.HueCycler
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*

object Chams : Module(
    name = "Chams",
    category = Category.RENDER,
    description = "Modify entity rendering"
) {
    private val page = setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val self = setting("Self", false, { page.value == Page.ENTITY_TYPE })
    private val all = setting("AllEntity", false, { page.value == Page.ENTITY_TYPE })
    private val experience = setting("Experience", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val arrows = setting("Arrows", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val throwable = setting("Throwable", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val items = setting("Items", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val crystals = setting("Crystals", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val players = setting("Players", true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val friends = setting("Friends", false, { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val sleeping = setting("Sleeping", false, { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val mobs = setting("Mobs", true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val passive = setting("PassiveMobs", false, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val neutral = setting("NeutralMobs", true, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val hostile = setting("HostileMobs", true, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })

    /* Rendering settings */
    private val throughWall = setting("ThroughWall", true, { page.value == Page.RENDERING })
    private val texture = setting("Texture", false, { page.value == Page.RENDERING })
    private val lightning = setting("Lightning", false, { page.value == Page.RENDERING })
    private val customColor = setting("CustomColor", false, { page.value == Page.RENDERING })
    private val rainbow = setting("Rainbow", false, { page.value == Page.RENDERING && customColor.value })
    private val r = setting("Red", 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val g = setting("Green", 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val b = setting("Blue", 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val a = setting("Alpha", 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value })

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var cycler = HueCycler(600)

    init {
        listener<RenderEntityEvent>(2000) {
            if (!checkEntityType(it.entity)) return@listener

            if (it.phase == Phase.PRE) {
                if (!texture.value) glDisable(GL_TEXTURE_2D)
                if (!lightning.value) glDisable(GL_LIGHTING)
                if (customColor.value) {
                    if (rainbow.value) cycler.currentRgba(a.value).setGLColor()
                    else glColor4f(r.value / 255.0f, g.value / 255.0f, b.value / 255.0f, a.value / 255.0f)
                    GlStateUtils.colorLock(true)
                    GlStateUtils.blend(true)
                    GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
                }
                if (throughWall.value) {
                    glDepthRange(0.0, 0.01)
                }
            }

            if (it.phase == Phase.PERI) {
                if (!texture.value) glEnable(GL_TEXTURE_2D)
                if (!lightning.value) glEnable(GL_LIGHTING)
                if (customColor.value) {
                    GlStateUtils.blend(false)
                    GlStateUtils.colorLock(false)
                    glColor4f(1f, 1f, 1f, 1f)
                }
                if (throughWall.value) {
                    glDepthRange(0.0, 1.0)
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase == TickEvent.Phase.START) cycler++
        }
    }

    private fun checkEntityType(entity: Entity): Boolean {
        return (self.value || entity != mc.player) && (all.value
                || experience.value && entity is EntityXPOrb
                || arrows.value && entity is EntityArrow
                || throwable.value && entity is EntityThrowable
                || items.value && entity is EntityItem
                || crystals.value && entity is EntityEnderCrystal
                || players.value && entity is EntityPlayer && EntityUtils.playerTypeCheck(entity, friends.value, sleeping.value)
                || mobTypeSettings(entity, mobs.value, passive.value, neutral.value, hostile.value))
    }
}
