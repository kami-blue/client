package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.ResolutionUpdateEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.EntityUtils.mobTypeSettings
import me.zeroeightsix.kami.util.color.HueCycler
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*

@Module.Info(
        name = "Chams",
        category = Module.Category.RENDER,
        description = "Modify entity rendering"
)
object Chams : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val self = register(Settings.booleanBuilder("Self").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE })
    private val all = register(Settings.booleanBuilder("AllEntity").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE })
    private val experience = register(Settings.booleanBuilder("Experience").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value })
    private val arrows = register(Settings.booleanBuilder("Arrows").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value })
    private val throwable = register(Settings.booleanBuilder("Throwable").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value })
    private val items = register(Settings.booleanBuilder("Items").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value })
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value })
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value })
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })

    /* Rendering settings */
    private val throughWall = register(Settings.booleanBuilder("ThroughWall").withValue(true).withVisibility { page.value == Page.RENDERING })
    private val texture = register(Settings.booleanBuilder("Texture").withValue(false).withVisibility { page.value == Page.RENDERING })
    private val lightning = register(Settings.booleanBuilder("Lightning").withValue(false).withVisibility { page.value == Page.RENDERING })
    private val customColor = register(Settings.booleanBuilder("CustomColor").withValue(false).withVisibility { page.value == Page.RENDERING })
    private val rainbow = register(Settings.booleanBuilder("Rainbow").withValue(false).withVisibility { page.value == Page.RENDERING && customColor.value })
    private val r = register(Settings.integerBuilder("Red").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val g = register(Settings.integerBuilder("Green").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && customColor.value && !rainbow.value })

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var cycler = HueCycler(600)
    private val frameBuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)

    init {
        listener<RenderEntityEvent.Pre>(2000) {
            if (it.entity == null || !checkEntityType(it.entity)) return@listener
            if (!texture.value) glDisable(GL_TEXTURE_2D)
            if (!lightning.value) glDisable(GL_LIGHTING)
            if (customColor.value) {
                if (rainbow.value) cycler.setCurrent()
                else glColor3f(r.value / 255f, g.value / 255f, b.value / 255f)
            }
            if (throughWall.value) {
                glPushMatrix()
                frameBuffer.bindFramebuffer(false)
            }
        }

        listener<RenderEntityEvent.Post>(500) {
            if (it.entity == null || !checkEntityType(it.entity)) return@listener
            if (!texture.value) glEnable(GL_TEXTURE_2D)
            if (!lightning.value) glEnable(GL_LIGHTING)
            if (customColor.value) {
                glColor4f(1f, 1f, 1f, 1f)
            }
            if (throughWall.value) {
                mc.framebuffer.bindFramebuffer(false)
                glPopMatrix()
            }
        }

        listener<RenderWorldEvent> {
            if (!throughWall.value) return@listener
            GlStateUtils.depth(false)
            glPushMatrix()
            frameBuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)
            frameBuffer.framebufferClear()
            mc.framebuffer.bindFramebuffer(false)
            glPopMatrix()
            GlStateUtils.depth(true)
        }

        listener<SafeTickEvent> {
            if (it.phase == TickEvent.Phase.START) cycler++
        }

        listener<ResolutionUpdateEvent> {
            frameBuffer.createFramebuffer(mc.displayWidth, mc.displayHeight)
        }
    }

    private fun checkEntityType(entity: Entity): Boolean {
        return (self.value || entity != mc.player) && (all.value
                || experience.value && entity is EntityXPOrb
                || arrows.value && entity is EntityArrow
                || throwable.value && entity is EntityThrowable
                || items.value && entity is EntityItem
                || players.value && entity is EntityPlayer && EntityUtils.playerTypeCheck(entity, friends.value, sleeping.value)
                || mobTypeSettings(entity, mobs.value, passive.value, neutral.value, hostile.value))
    }
}