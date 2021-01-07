package me.zeroeightsix.kami.module.modules.render

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
    name = "ESP",
    category = Category.RENDER,
    description = "Highlights entities"
) {
    private val page = setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val all = setting("AllEntity", false, { page.value == Page.ENTITY_TYPE })
    private val experience = setting("Experience", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val arrows = setting("Arrows", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val throwable = setting("Throwable", false, { page.value == Page.ENTITY_TYPE && !all.value })
    private val items = setting("Items", true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val players = setting("Players", true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val friends = setting("Friends", false, { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val sleeping = setting("Sleeping", false, { page.value == Page.ENTITY_TYPE && !all.value && players.value })
    private val mobs = setting("Mobs", true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val passive = setting("PassiveMobs", false, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val neutral = setting("NeutralMobs", true, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val hostile = setting("HostileMobs", true, { page.value == Page.ENTITY_TYPE && !all.value && mobs.value })
    private val invisible = setting("Invisible", true, { page.value == Page.ENTITY_TYPE && !all.value })
    private val range = setting("Range", 64, 8..128, 8, { page.value == Page.ENTITY_TYPE })

    /* Rendering settings */
    private val mode = setting("Mode", ESPMode.SHADER, { page.value == Page.RENDERING })
    private val hideOriginal = setting("HideOriginal", false, { page.value == Page.RENDERING && mode.value == ESPMode.SHADER })
    private val filled = setting("Filled", false, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val outline = setting("Outline", true, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val r = setting("Red", 155, 0..255, 1, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val g = setting("Green", 144, 0..255, 1, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val b = setting("Blue", 255, 0..255, 1, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val aFilled = setting("FilledAlpha", 63, 0..255, 1, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val aOutline = setting("OutlineAlpha", 255, 0..255, 1, { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) })
    private val blurRadius = setting("BlurRadius", 0f, 0f..16f, 0.5f, { page.value == Page.RENDERING && mode.value == ESPMode.SHADER })
    private val width = setting("Width", 2f, 1f..8f, 0.25f, { page.value == Page.RENDERING })

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
        mode.listeners.add {
            drawingOutline = false
            drawNametag = false
            resetGlow()
        }

        listener<RenderEntityEvent> {
            if (mode.value != ESPMode.SHADER || mc.renderManager.renderOutlines || !entityList.contains(it.entity)) return@listener

            if (it.phase == Phase.PRE && hideOriginal.value) {
                // Steal it from Minecraft rendering kek
                prepareFrameBuffer()
                drawNametag = true
            }

            if (it.phase == Phase.PERI) {
                if (!hideOriginal.value) {
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
        listener<RenderWorldEvent> {
            if (mc.renderManager.options == null) return@listener
            when (mode.value) {
                ESPMode.BOX -> {
                    val colour = ColorHolder(r.value, g.value, b.value)
                    val renderer = ESPRenderer()
                    renderer.aFilled = if (filled.value) aFilled.value else 0
                    renderer.aOutline = if (outline.value) aOutline.value else 0
                    renderer.thickness = width.value
                    for (entity in entityList) {
                        renderer.add(entity, colour)
                    }
                    renderer.render(true)
                }

                else -> {
                    // other modes, such as GLOW, use onUpdate()
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            entityList.clear()
            entityList.addAll(getEntityList())

            if (mode.value == ESPMode.GLOW) {
                if (entityList.isNotEmpty()) {
                    for (shader in mc.renderGlobal.entityOutlineShader.listShaders) {
                        shader.shaderManager.getShaderUniform("Radius")?.set(width.value)
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
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val entityList = ArrayList<Entity>()
        if (all.value) {
            for (entity in world.loadedEntityList) {
                if (entity == mc.renderViewEntity) continue
                if (mc.player.getDistance(entity) > range.value) continue
                entityList.add(entity)
            }
        } else {
            entityList.addAll(getTargetList(player, mob, invisible.value, range.value.toFloat(), ignoreSelf = false))
            for (entity in world.loadedEntityList) {
                if (entity == player) continue
                if (mc.player.getDistance(entity) > range.value) continue
                if (entity is EntityXPOrb && experience.value
                        || entity is EntityArrow && arrows.value
                        || entity is EntityThrowable && throwable.value
                        || entity is EntityItem && items.value) {
                    entityList.add(entity)
                }
            }
        }
        return entityList
    }

    private fun setShaderSettings(shader: Shader) {
        shader.shaderManager.getShaderUniform("color")?.set(r.value / 255f, g.value / 255f, b.value / 255f)
        shader.shaderManager.getShaderUniform("outlineAlpha")?.set(if (outline.value) aOutline.value / 255f else 0f)
        shader.shaderManager.getShaderUniform("filledAlpha")?.set(if (filled.value) aFilled.value / 255f else 0f)
        shader.shaderManager.getShaderUniform("width")?.set(width.value)
        shader.shaderManager.getShaderUniform("Radius")?.set(blurRadius.value)
    }

    override fun onDisable() {
        resetGlow()
    }

    private fun resetGlow() {
        if (mc.player == null) return

        for (shader in mc.renderGlobal.entityOutlineShader.listShaders) {
            shader.shaderManager.getShaderUniform("Radius")?.set(2f) // default radius
        }

        for (entity in mc.world.loadedEntityList) {
            entity.isGlowing = false
        }
    }
}