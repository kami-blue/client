package org.kamiblue.client.module.modules.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Shader
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RenderEntityEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.mixin.extension.*
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.EntityUtils.getTargetList
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.KamiTessellator
import org.kamiblue.client.util.graphics.ShaderHelper
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.GL_MODELVIEW
import org.lwjgl.opengl.GL11.GL_PROJECTION

internal object ESP : Module(
    name = "ESP",
    category = Category.RENDER,
    description = "Highlights entities"
) {
    private val page by setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val all by setting("All Entities", false, { page == Page.ENTITY_TYPE })
    private val experience by setting("Experience", false, { page == Page.ENTITY_TYPE && !all })
    private val arrows by setting("Arrows", false, { page == Page.ENTITY_TYPE && !all })
    private val throwable by setting("Throwable", false, { page == Page.ENTITY_TYPE && !all })
    private val items by setting("Items", true, { page == Page.ENTITY_TYPE && !all })
    private val players by setting("Players", true, { page == Page.ENTITY_TYPE && !all })
    private val friends by setting("Friends", false, { page == Page.ENTITY_TYPE && !all && players })
    private val sleeping by setting("Sleeping", false, { page == Page.ENTITY_TYPE && !all && players })
    private val mobs by setting("Mobs", true, { page == Page.ENTITY_TYPE && !all })
    private val passive by setting("Passive Mobs", false, { page == Page.ENTITY_TYPE && !all && mobs })
    private val neutral by setting("Neutral Mobs", true, { page == Page.ENTITY_TYPE && !all && mobs })
    private val hostile by setting("Hostile Mobs", true, { page == Page.ENTITY_TYPE && !all && mobs })
    private val invisible by setting("Invisible", true, { page == Page.ENTITY_TYPE && !all })
    private val range by setting("Range", 32.0f, 8.0f..64.0f, 0.5f, { page == Page.ENTITY_TYPE })

    /* Rendering settings */
    private val mode = setting("Mode", ESPMode.SHADER, { page == Page.RENDERING })
    private val hideOriginal by setting("Hide Original", false, { page == Page.RENDERING && mode.value == ESPMode.SHADER })
    private val filled by setting("Filled", false, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val outline by setting("Outline", true, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val r by setting("Red", 155, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val g by setting("Green", 144, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val b by setting("Blue", 255, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val aFilled by setting("Filled Alpha", 63, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val aOutline by setting("Outline Alpha", 255, 0..255, 1, { page == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val blurRadius by setting("Blur Radius", 0f, 0f..16f, 0.5f, { page == Page.RENDERING && mode.value == ESPMode.SHADER })
    private val width by setting("Width", 2f, 1f..8f, 0.25f, { page == Page.RENDERING })

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private enum class ESPMode {
        BOX, GLOW, SHADER
    }

    private val entityList = LinkedHashSet<Entity>()

    private val shaderHelper = ShaderHelper(ResourceLocation("shaders/post/esp_outline.json"), "final")
    private val frameBuffer = shaderHelper.getFrameBuffer("final")

    init {
        listener<RenderEntityEvent.All> {
            if (it.phase == Phase.PRE
                && mode.value == ESPMode.SHADER
                && !mc.renderManager.renderOutlines
                && hideOriginal
                && entityList.contains(it.entity)) {
                it.cancel()
            }
        }

        safeListener<EntityViewRenderEvent.FogColors> { event ->
            shaderHelper.shader?.listFrameBuffers?.forEach {
                it.setFramebufferColor(event.red, event.green, event.blue, 0.0f)
            }
        }

        safeListener<RenderWorldEvent>(69420) {
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
                ESPMode.SHADER -> {
                    drawEntities()
                    drawShader()
                }
                else -> {
                    // Glow Mode
                }
            }
        }
    }

    private fun drawEntities() {
        // Clean up the frame buffer and bind it
        frameBuffer?.framebufferClear()
        frameBuffer?.bindFramebuffer(false)

        val prevRenderOutlines = mc.renderManager.renderOutlines

        GlStateUtils.texture2d(true)
        GlStateUtils.cull(true)

        // Draw the entities into the framebuffer
        for (entity in entityList) {
            val renderer = mc.renderManager.getEntityRenderObject<Entity>(entity) ?: continue

            val partialTicks = KamiTessellator.pTicks()
            val yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks
            val pos = EntityUtils.getInterpolatedPos(entity, partialTicks)
                .subtract(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)

            renderer.setRenderOutlines(true)
            renderer.doRender(entity, pos.x, pos.y, pos.z, yaw, partialTicks)
            renderer.setRenderOutlines(prevRenderOutlines)
        }

        GlStateUtils.texture2d(false)
    }

    private fun drawShader() {
        // Push matrix
        GlStateManager.matrixMode(GL_PROJECTION)
        GlStateManager.pushMatrix()
        GlStateManager.matrixMode(GL_MODELVIEW)
        GlStateManager.pushMatrix()

        shaderHelper.shader?.render(KamiTessellator.pTicks())

        // Re-enable blend because shader rendering will disable it at the end
        GlStateUtils.blend(true)
        GlStateUtils.depth(false)

        // Draw it on the main frame buffer
        mc.framebuffer.bindFramebuffer(false)
        frameBuffer?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)

        // Revert states
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        GlStateUtils.texture2d(false)
        GlStateManager.depthMask(false)
        GlStateUtils.cull(false)

        // Revert matrix
        GlStateManager.matrixMode(GL_PROJECTION)
        GlStateManager.popMatrix()
        GlStateManager.matrixMode(GL_MODELVIEW)
        GlStateManager.popMatrix()
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            entityList.clear()
            entityList.addAll(getEntityList())

            when (mode.value) {
                ESPMode.GLOW -> {
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
                }
                ESPMode.SHADER -> {
                    shaderHelper.shader?.let {
                        for (shader in it.listShaders) {
                            setShaderSettings(shader)
                        }
                    }
                }
                else -> {
                    // Box Mode
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