package org.kamiblue.client.module.modules.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Shader
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RenderEntityEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.mixin.extension.entityOutlineShader
import org.kamiblue.client.mixin.extension.listShaders
import org.kamiblue.client.mixin.extension.renderOutlines
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.getTargetList
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.graphics.KamiTessellator
import org.kamiblue.client.util.graphics.ShaderHelper
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*

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

        listener<RenderWorldEvent>(69420) {
            if (mode.value != ESPMode.SHADER) return@listener

            GlStateManager.matrixMode(GL_PROJECTION)
            glPushMatrix()
            GlStateManager.matrixMode(GL_MODELVIEW)
            glPushMatrix()

            shaderHelper.shader?.render(KamiTessellator.pTicks())

            // Re-enable blend because shader rendering will disable it at the end
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()

            // Draw it on the main frame buffer
            mc.framebuffer.bindFramebuffer(false)
            frameBuffer?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)

            // Clean up the frame buffer
            frameBuffer?.framebufferClear()
            mc.framebuffer.bindFramebuffer(false)

            GlStateManager.disableCull()
            GlStateManager.enableBlend()
            GlStateManager.enableDepth()
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)

            GlStateManager.matrixMode(GL_PROJECTION)
            glPopMatrix()
            GlStateManager.matrixMode(GL_MODELVIEW)
            glPopMatrix()
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