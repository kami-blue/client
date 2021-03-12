package org.kamiblue.client.module.modules.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.events.RenderEntityEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.EntityUtils.mobTypeSettings
import org.kamiblue.client.util.color.HueCycler
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*

internal object Chams : Module(
    name = "Chams",
    category = Category.RENDER,
    description = "Modify entity rendering"
) {
    private val page = setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val self by setting("Self", false, page.atValue(Page.ENTITY_TYPE))
    private val all0 = setting("All Entities", false, page.atValue(Page.ENTITY_TYPE))
    private val all by all0
    private val experience by setting("Experience", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val arrows by setting("Arrows", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val throwable by setting("Throwable", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val items by setting("Items", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val crystals by setting("Crystals", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val players by setting("Players", true, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val friends by setting("Friends", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val sleeping by setting("Sleeping", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val mobs by setting("Mobs", true, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val passive by setting("Passive Mobs", false, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val neutral by setting("Neutral Mobs", true, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())
    private val hostile by setting("Hostile Mobs", true, page.atValue(Page.ENTITY_TYPE) and all0.atFalse())

    /* Rendering settings */
    private val throughWall by setting("Through Wall", true, page.atValue(Page.RENDERING))
    private val texture by setting("Texture", false, page.atValue(Page.RENDERING))
    private val lightning by setting("Lightning", false, page.atValue(Page.RENDERING))
    private val customColor0 = setting("Custom Color", false, page.atValue(Page.RENDERING))
    private val customColor by customColor0
    private val rainbow0 = setting("Rainbow", false, page.atValue(Page.RENDERING) and customColor0.atTrue())
    private val rainbow by rainbow0
    private val r by setting("Red", 255, 0..255, 1, page.atValue(Page.RENDERING) and customColor0.atTrue() and rainbow0.atFalse())
    private val g by setting("Green", 255, 0..255, 1, page.atValue(Page.RENDERING) and customColor0.atTrue() and rainbow0.atFalse())
    private val b by setting("Blue", 255, 0..255, 1, page.atValue(Page.RENDERING) and customColor0.atTrue() and rainbow0.atFalse())
    private val a by setting("Alpha", 160, 0..255, 1, page.atValue(Page.RENDERING) and customColor0.atTrue())

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var cycler = HueCycler(600)

    init {
        listener<RenderEntityEvent.All>(2000) {
            if (!checkEntityType(it.entity)) return@listener

            when (it.phase) {
                Phase.PRE -> {
                    if (throughWall) glDepthRange(0.0, 0.01)
                }
                Phase.PERI -> {
                    if (throughWall) glDepthRange(0.0, 1.0)
                }
                else -> {
                    // Doesn't need to do anything on post phase
                }
            }
        }

        listener<RenderEntityEvent.Model> {
            if (!checkEntityType(it.entity)) return@listener

            when (it.phase) {
                Phase.PRE -> {
                    if (!texture) glDisable(GL_TEXTURE_2D)
                    if (!lightning) glDisable(GL_LIGHTING)
                    if (customColor) {
                        if (rainbow) cycler.currentRgba(a).setGLColor()
                        else glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f)

                        GlStateUtils.blend(true)
                        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
                    }
                }
                Phase.POST -> {
                    if (!texture) glEnable(GL_TEXTURE_2D)
                    if (!lightning) glEnable(GL_LIGHTING)
                    if (customColor) {
                        GlStateUtils.blend(false)
                        glColor4f(1f, 1f, 1f, 1f)
                    }
                }
                else -> {
                    // RenderEntityEvent.Model doesn't have peri phase
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase == TickEvent.Phase.START) cycler++
        }
    }

    private fun checkEntityType(entity: Entity) =
        (self || entity != mc.player) && (
            all
                || experience && entity is EntityXPOrb
                || arrows && entity is EntityArrow
                || throwable && entity is EntityThrowable
                || items && entity is EntityItem
                || crystals && entity is EntityEnderCrystal
                || players && entity is EntityPlayer && EntityUtils.playerTypeCheck(entity, friends, sleeping)
                || mobTypeSettings(entity, mobs, passive, neutral, hostile)
            )
}
