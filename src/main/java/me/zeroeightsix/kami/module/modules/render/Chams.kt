package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.EntityUtils.mobTypeSettings
import me.zeroeightsix.kami.util.color.HueCycler
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import org.lwjgl.opengl.GL11.*

/**
 * Created by 086 on 12/12/2017.
 * Updated by Xiaro on 05/05/20
 */
@Module.Info(
        name = "Chams",
        category = Module.Category.RENDER,
        description = "Modify entity rendering"
)
class Chams : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val self = register(Settings.booleanBuilder("Self").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val all = register(Settings.booleanBuilder("AllEntity").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val experience = register(Settings.booleanBuilder("Experience").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val arrows = register(Settings.booleanBuilder("Arrows").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val throwable = register(Settings.booleanBuilder("Throwable").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val items = register(Settings.booleanBuilder("Items").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && players.value }.build())
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value }.build())

    /* Rendering settings */
    private val throughWall = register(Settings.booleanBuilder("ThroughWall").withValue(true).withVisibility { page.value == Page.RENDERING }.build())
    private val texture = register(Settings.booleanBuilder("Texture").withValue(false).withVisibility { page.value == Page.RENDERING }.build())
    private val lightning = register(Settings.booleanBuilder("Lightning").withValue(false).withVisibility { page.value == Page.RENDERING }.build())
    private val customColor = register(Settings.booleanBuilder("CustomColor").withValue(false).withVisibility { page.value == Page.RENDERING }.build())
    private val rainbow = register(Settings.booleanBuilder("Rainbow").withValue(false).withVisibility { page.value == Page.RENDERING && customColor.value }.build())
    private val r = register(Settings.integerBuilder("Red").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && customColor.value && !rainbow.value }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && customColor.value && !rainbow.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && customColor.value && !rainbow.value }.build())

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var cycler = HueCycler(600)

    override fun onUpdate() {
        cycler++
    }

    @EventHandler
    private val preRenderListener = Listener(EventHook { event: RenderEntityEvent.Pre ->
        if (event.entity == null || !checkEntityType(event.entity)) return@EventHook
        if (!texture.value) glDisable(GL_TEXTURE_2D)
        if (!lightning.value) glDisable(GL_LIGHTING)
        if (throughWall.value) {
            glEnable(GL_POLYGON_OFFSET_FILL)
            glPolygonOffset(1.0f, -9999999.0f)
        }
        if (customColor.value) {
            if (rainbow.value) cycler.setCurrent()
            else glColor3f(r.value / 255f, g.value / 255f, b.value / 255f)
        }
    })

    @EventHandler
    private val postRenderListener = Listener(EventHook { event: RenderEntityEvent.Post ->
        if (event.entity == null || !checkEntityType(event.entity)) return@EventHook
        if (!texture.value) glEnable(GL_TEXTURE_2D)
        if (!lightning.value) glEnable(GL_LIGHTING)
        if (throughWall.value) {
            glDisable(GL_POLYGON_OFFSET_FILL)
            glPolygonOffset(1.0f, 9999999.0f)
        }
        if (customColor.value) glColor4f(1f, 1f, 1f, 1f)
    })

    private fun checkEntityType(entity: Entity): Boolean {
        return (self.value || entity != mc.player) && (all.value
                || entity is EntityXPOrb && experience.value
                || entity is EntityArrow && arrows.value
                || entity is EntityThrowable && throwable.value
                || entity is EntityItem && items.value
                || entity is EntityPlayer && players.value && EntityUtils.playerTypeCheck(entity, friends.value, sleeping.value)
                || mobTypeSettings(entity, mobs.value, passive.value, neutral.value, hostile.value))
    }
}