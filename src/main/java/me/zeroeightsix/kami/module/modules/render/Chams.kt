package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
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
    name = KamiLang.get("module.modules.render.Chams.Chams"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.Chams.ModifyEntityRendering")
) {
    private val page = setting(KamiLang.get("module.modules.render.Chams.Page"), Page.ENTITY_TYPE)

    /* Entity type settings */
    private val self = setting(KamiLang.get("module.modules.render.Chams.Self"), false, { page.value == Page.ENTITY_TYPE })
    private val all = setting(KamiLang.get("module.modules.render.Chams.Allentity"), false, { page.value == Page.ENTITY_TYPE })
    private val experience = setting(KamiLang.get("module.modules.render.Chams.Experience"), false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val arrows = setting(KamiLang.get("module.modules.render.Chams.Arrows"), false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val throwable = setting(KamiLang.get("module.modules.render.Chams.Throwable"), false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val items = setting(KamiLang.get("module.modules.render.Chams.Items"), false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val crystals = setting(KamiLang.get("module.modules.render.Chams.Crystals"), false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val players = setting(KamiLang.get("module.modules.render.Chams.Players"), true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val friends = setting(KamiLang.get("module.modules.render.Chams.Friends"), false, { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val sleeping = setting(KamiLang.get("module.modules.render.Chams.Sleeping"), false, { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val mobs = setting(KamiLang.get("module.modules.render.Chams.Mobs"), true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val passive = setting(KamiLang.get("module.modules.render.Chams.Passivemobs"), false, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val neutral = setting(KamiLang.get("module.modules.render.Chams.Neutralmobs"), true, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val hostile = setting(KamiLang.get("module.modules.render.Chams.Hostilemobs"), true, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })

    /* Rendering settings */
    private val throughWall = setting(KamiLang.get("module.modules.render.Chams.Throughwall"), true, { page.value == Page.RENDERING })
    private val texture = setting(KamiLang.get("module.modules.render.Chams.Texture"), false, { page.value == Page.RENDERING })
    private val lightning = setting(KamiLang.get("module.modules.render.Chams.Lightning"), false, { page.value == Page.RENDERING })
    private val customColor = setting(KamiLang.get("module.modules.render.Chams.Customcolor"), false, { page.value == Page.RENDERING })
    private val rainbow = setting(KamiLang.get("module.modules.render.Chams.Rainbow"), false, { page.value == Page.RENDERING && customColor.value })
    private val r = setting(KamiLang.get("module.modules.render.Chams.Red"), 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val g = setting(KamiLang.get("module.modules.render.Chams.Green"), 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val b = setting(KamiLang.get("module.modules.render.Chams.Blue"), 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value && !rainbow.value })
    private val a = setting(KamiLang.get("module.modules.render.Chams.Alpha"), 255, 0..255, 1, { page.value == Page.RENDERING && customColor.value })

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
