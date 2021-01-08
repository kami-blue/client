package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.event.events.RenderShaderEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.mixin.extension.entityOutlineShader
import me.zeroeightsix.kami.mixin.extension.listShaders
import me.zeroeightsix.kami.mixin.extension.renderOutlines
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.graphics.ShaderHelper
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Shader
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

object ESP : Module(
    name = KamiLang.get("module.modules.render.ESP.Esp"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.ESP.HighlightsEntities")
) {
    private val page by setting(KamiLang.get("module.modules.render.ESP.Page"), Page.ENTITY_TYPE)

    /* Entity type settings */
    private val all by setting(KamiLang.get("module.modules.render.ESP.Allentity"), false, { page == Page.ENTITY_TYPE })
    private val experience by setting(KamiLang.get("module.modules.render.ESP.Experience"), false, { page == Page.ENTITY_TYPE && !all })
    private val arrows by setting(KamiLang.get("module.modules.render.ESP.Arrows"), false, { page == Page.ENTITY_TYPE && !all })
    private val throwable by setting(KamiLang.get("module.modules.render.ESP.Throwable"), false, { page == Page.ENTITY_TYPE && !all })
    private val items by setting(KamiLang.get("module.modules.render.ESP.Items"), true, { page == Page.ENTITY_TYPE && !all })
    private val players by setting(KamiLang.get("module.modules.render.ESP.Players"), true, { page == Page.ENTITY_TYPE && !all })
    private val friends by setting(KamiLang.get("module.modules.render.ESP.Friends"), false, { page == Page.ENTITY_TYPE && !all && players })
    private val sleeping by setting(KamiLang.get("module.modules.render.ESP.Sleeping"), false, { page == Page.ENTITY_TYPE && !all && players })
    private val mobs by setting(KamiLang.get("module.modules.render.ESP.Mobs"), true, { page == Page.ENTITY_TYPE && !all })
    private val passive by setting(KamiLang.get("module.modules.render.ESP.Passivemobs"), false, { page == Page.ENTITY_TYPE && !all && mobs })
    private val neutral by setting(KamiLang.get("module.modules.render.ESP.Neutralmobs"), true, { page == Page.ENTITY_TYPE && !all && mobs })
    private val hostile by setting(KamiLang.get("module.modules.render.ESP.Hostilemobs"), true, { page == Page.ENTITY_TYPE && !all && mobs })
    private val invisible by setting(KamiLang.get("module.modules.render.ESP.Invisible"), true, { page == Page.ENTITY_TYPE && !all })
    private val range by setting(KamiLang.get("module.modules.render.ESP.Range"), 32.0f, 8.0f..64.0f, 0.5f, { page == Page.ENTITY_TYPE })

    /* Rendering settings */
    private val mode = setting(KamiLang.get("module.modules.render.ESP.Mode"), ESPMode.SHADER, { page == Page.RENDERING })
    private val hideOriginal by setting(KamiLang.get("module.modules.render.ESP.Hideoriginal"), false, { page == Page.RENDERING && mode.value == ESPMode.SHADER })
    private val filled by setting(KamiLang.get("module.modules.render.ESP.Filled"), false, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val outline by setting(KamiLang.get("module.modules.render.ESP.Outline"), true, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val r by setting(KamiLang.get("module.modules.render.ESP.Red"), 155, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val g by setting(KamiLang.get("module.modules.render.ESP.Green"), 144, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val b by setting(KamiLang.get("module.modules.render.ESP.Blue"), 255, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val aFilled by setting(KamiLang.get("module.modules.render.ESP.Filledalpha"), 63, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val aOutline by setting(KamiLang.get("module.modules.render.ESP.Outlinealpha"), 255, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val blurRadius by setting(KamiLang.get("module.modules.render.ESP.Blurradius"), 0f, 0f..16f, 0.5f, { page == Page.RENDERING && mode.value == ESPMode.SHADER })
    private val width by setting(KamiLang.get("module.modules.render.ESP.Width"), 2f, 1f..8f, 0.25f, { page == Page.RENDERING })

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private enum class ESPMode {
        BOX, GLOW, SHADER
    }

    private val entityList = HashSet<Entity>()

    var drawingOutline = false; private set
    var drawNametag = false; private set
    private val shaderHelper = ShaderHelper(ResourceLocation("shaders/post/esp_outline.json"), "final")
    val frameBuffer = shaderHelper.getFrameBuffer("final")

    init {
        listener<RenderEntityEvent> {
            if (mode.value != ESPMode.SHADER || mc.renderManager.renderOutlines || !entityList.contains(it.entity)) return@listener

            if (it.phase == Phase.PRE && hideOriginal) {
                // Steal it from Minecraft rendering kek
                prepareFrameBuffer()
                drawNametag = true
            }

            if (it.phase == Phase.PERI) {
                if (!hideOriginal) {
                    prepareFrameBuffer()
                    mc.renderManager.getEntityRenderObject<Entity>(it.entity)?.doRender(it.entity, it.x, it.y, it.z, it.yaw, it.partialTicks)
                }

                mc.framebuffer.bindFramebuffer(false)
                GlStateManager.disableOutlineMode()
                drawingOutline = false
                drawNametag = false
            }
        }

        listener<RenderShaderEvent> {
            if (mode.value != ESPMode.SHADER) return@listener

            frameBuffer?.bindFramebuffer(false)
            shaderHelper.shader?.render(KamiTessellator.pTicks())

            // Draw it on the main frame buffer
            mc.framebuffer.bindFramebuffer(false)
            GlStateManager.disableDepth()
            // Re-enable blend because shader rendering will disable it at the end
            GlStateManager.enableBlend()
            frameBuffer?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)
            GlStateManager.disableBlend()
            GlStateManager.enableDepth()

            // Clean up the frame buffer
            frameBuffer?.framebufferClear()
            mc.framebuffer.bindFramebuffer(true)
        }
    }

    private fun prepareFrameBuffer() {
        drawingOutline = true
        GlStateManager.enableOutlineMode(0xFFFFFF)
        frameBuffer?.bindFramebuffer(false)
    }

    init {
        safeListener<RenderWorldEvent> {
            if (mc.renderManager.options == null) return@safeListener

            when (mode.value) {
                ESPMode.BOX -> {
                    val color = ColorHolder(r, g, b)
                    val renderer = ESPRenderer()
                    renderer.aFilled = if (filled) aFilled else 0
                    renderer.aOutline = if (outline) aOutline else 0
                    renderer.thickness = width
                    for (entity in entityList) {
                        renderer.add(entity, color)
                    }
                    renderer.render(true)
                }

                else -> {
                    // Glow and Shader mode
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            entityList.clear()
            entityList.addAll(getEntityList())

            if (mode.value == ESPMode.GLOW) {
                if (entityList.isNotEmpty()) {
                    for (shader in mc.renderGlobal.entityOutlineShader.listShaders) {
                        shader.shaderManager.getShaderUniform("Radius")?.set(width)
                    }

                    for (entity in world.loadedEntityList) { // Set glow for entities in the list. Remove glow for entities not in the list
                        entity.isGlowing = entityList.contains(entity)
                    }
                } else {
                    resetGlow()
                }
            } else if (mode.value == ESPMode.SHADER) {
                shaderHelper.shader?.let {
                    for (shader in it.listShaders) {
                        setShaderSettings(shader)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.getEntityList(): List<Entity> {
        val playerSettings = arrayOf(players, friends, sleeping)
        val mob = arrayOf(mobs, passive, neutral, hostile)
        val entityList = ArrayList<Entity>()
        if (all) {
            for (entity in world.loadedEntityList) {
                if (entity == mc.renderViewEntity) continue
                if (player.getDistance(entity) > range) continue
                entityList.add(entity)
            }
        } else {
            entityList.addAll(getTargetList(playerSettings, mob, invisible, range, ignoreSelf = false))
            for (entity in world.loadedEntityList) {
                if (entity == player) continue
                if (player.getDistance(entity) > range) continue
                if (entity is EntityXPOrb && experience
                        || entity is EntityArrow && arrows
                        || entity is EntityThrowable && throwable
                        || entity is EntityItem && items) {
                    entityList.add(entity)
                }
            }
        }
        return entityList
    }

    private fun setShaderSettings(shader: Shader) {
        shader.shaderManager.getShaderUniform("color")?.set(r / 255f, g / 255f, b / 255f)
        shader.shaderManager.getShaderUniform("outlineAlpha")?.set(if (outline) aOutline / 255f else 0f)
        shader.shaderManager.getShaderUniform("filledAlpha")?.set(if (filled) aFilled / 255f else 0f)
        shader.shaderManager.getShaderUniform("width")?.set(width)
        shader.shaderManager.getShaderUniform("Radius")?.set(blurRadius)
    }

    init {
        onDisable {
            resetGlow()
        }

        mode.listeners.add {
            drawingOutline = false
            drawNametag = false
            resetGlow()
        }
    }

    private fun resetGlow() {
        runSafe {
            for (shader in mc.renderGlobal.entityOutlineShader.listShaders) {
                shader.shaderManager.getShaderUniform("Radius")?.set(2f) // default radius
            }

            for (entity in world.loadedEntityList) {
                entity.isGlowing = false
            }
        }
    }
}