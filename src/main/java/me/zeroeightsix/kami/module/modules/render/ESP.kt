package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourHolder
import me.zeroeightsix.kami.util.ESPRenderer
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.shader.Shader
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import java.util.function.Consumer

/**
 * Created by 086 on 14/12/2017.
 * Updated by d1gress/Qther on 27/11/2019.
 * Kurisu Makise is cute
 * Updated by dominikaaaa on 19/04/20
 * Updated by Xiaro on 31/07/20
 */
@Module.Info(
        name = "ESP",
        category = Module.Category.RENDER,
        description = "Highlights entities")
class ESP : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val all = register(Settings.booleanBuilder("AllEntity").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val experience = register(Settings.booleanBuilder("Experience").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val arrows = register(Settings.booleanBuilder("Arrows").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val throwable = register(Settings.booleanBuilder("Throwable").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val items = register(Settings.booleanBuilder("Items").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && players.value }.build())
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value && mobs.value }.build())
    private val invisible = register(Settings.booleanBuilder("Invisible").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && !all.value }.build())
    private val range = register(Settings.integerBuilder("Range").withValue(64).withRange(1, 128).withVisibility { page.value == Page.ENTITY_TYPE }.build())

    /* Rendering settings */
    private val mode = register(Settings.enumBuilder(ESPMode::class.java).withName("Mode").withValue(ESPMode.BOX).withVisibility { page.value == Page.RENDERING }.build())
    private val radiusValue = register(Settings.integerBuilder("Width").withMinimum(1).withMaximum(100).withValue(25).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.GLOW }.build())
    private val filled = register(Settings.booleanBuilder("Filled").withValue(false).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(63).withRange(0, 255).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(2.0f).withRange(0.0f, 8.0f).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.BOX }.build())

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private enum class ESPMode {
        BOX, GLOW
    }

    private var entityList: Array<Entity>? = null

    override fun onWorldRender(event: RenderEvent) {
        if (Wrapper.getMinecraft().getRenderManager().options == null || entityList == null) return
        when (mode.value) {
            ESPMode.BOX -> {
                val colour = ColourHolder(r.value, g.value, b.value)
                val renderer = ESPRenderer(event.partialTicks)
                renderer.aFilled = if (filled.value) aFilled.value else 0
                renderer.aOutline = if (outline.value) aOutline.value else 0
                renderer.thickness = thickness.value
                for (e in entityList!!) {
                    renderer.add(e, colour)
                }
                renderer.render()
            }

            else -> {

            }
        }
    }

    override fun onUpdate() {
        entityList = getEntityList()

        if (mode.value == ESPMode.GLOW) {
            if (entityList?.isNotEmpty() == true) {
                mc.renderGlobal.entityOutlineShader.listShaders.forEach(Consumer { shader: Shader ->
                    shader.shaderManager.getShaderUniform("Radius")?.set(radiusValue.value / 50f)
                })
                for (e in mc.world.loadedEntityList) { // Set grow for entities in the list. Remove grow for entities not in the list
                    e.isGlowing = entityList!!.contains(e)
                }
            } else {
                resetGlow()
            }
        }
    }

    public override fun onDisable() {
        resetGlow()
    }

    private fun getEntityList(): Array<Entity> {
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val entityList = ArrayList<Entity>()
        if (all.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity == mc.player) continue
                if (mc.player.getDistance(entity) > range.value) continue
                entityList.add(entity)
            }
        } else {
            entityList.addAll(getTargetList(player, mob, true, invisible.value, range.value.toFloat()))
            for (entity in mc.world.loadedEntityList) {
                if (entity == mc.player) continue
                if (mc.player.getDistance(entity) > range.value) continue
                if (entity is EntityXPOrb && experience.value
                        || entity is EntityArrow && arrows.value
                        || entity is EntityThrowable && throwable.value
                        || entity is EntityItem && items.value) {
                    entityList.add(entity)
                }
            }
        }
        return entityList.toTypedArray()
    }

    private fun resetGlow() {
        if (mc.player == null) return
        mc.renderGlobal.entityOutlineShader.listShaders.forEach(Consumer { shader: Shader ->
            val radius = shader.shaderManager.getShaderUniform("Radius")
            radius?.set(2f) // default radius
        })
        for (e in mc.world.loadedEntityList) {
            e.isGlowing = false
        }
        mc.player.isGlowing = false
    }

    init {
        mode.settingListener = Setting.SettingListeners {
            resetGlow()
        }
    }
}