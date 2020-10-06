package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.client.shader.ShaderLinkHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.util.ResourceLocation

@Module.Info(
        name = "ESP",
        category = Module.Category.RENDER,
        description = "Highlights entities"
)
object ESP : Module() {
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
    private val mode = register(Settings.enumBuilder(ESPMode::class.java, "Mode").withValue(ESPMode.SHADER).withVisibility { page.value == Page.RENDERING }.build())
    private val hideOriginal = register(Settings.booleanBuilder("HideOriginal").withValue(false).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.SHADER }.build())
    private val filled = register(Settings.booleanBuilder("Filled").withValue(false).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(63).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(255).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING && (mode.value == ESPMode.BOX || mode.value == ESPMode.SHADER) }.build())
    private val blurRadius = register(Settings.floatBuilder("BlurRadius").withValue(0f).withRange(0f, 16f).withStep(0.5f).withVisibility { page.value == Page.RENDERING && mode.value == ESPMode.SHADER }.build())
    private val width = register(Settings.floatBuilder("Width").withValue(2f).withRange(1f, 8f).withStep(0.25f).withVisibility { page.value == Page.RENDERING }.build())

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private enum class ESPMode {
        BOX, GLOW, SHADER
    }

    private val entityList = HashSet<Entity>()

    private val shader: ShaderGroup?
    private val frameBuffer: Framebuffer?
    private var prevWidth = mc.displayWidth
    private var prevHeight = mc.displayHeight

    init {
        mode.settingListener = Setting.SettingListeners {
            resetGlow()
        }

        shader = if (OpenGlHelper.shadersSupported) {
            try {
                val resourceLocation = ResourceLocation("shaders/post/esp_outline.json")
                ShaderLinkHelper.setNewStaticShaderLinkHelper()
                ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, resourceLocation).also {
                    it.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                }
            } catch (e: Exception) {
                MessageSendHelper.sendErrorMessage("$chatName Error loading Shader: \"${e.message}\", check the log for more information!")
                KamiMod.log.warn("$chatName Failed loading Shader")
                e.printStackTrace()
                null
            }.also {
                frameBuffer = it?.getFramebufferRaw("final")
            }
        } else {
            frameBuffer = null
            null
        }
    }

    @EventHandler
    private val preRenderListener = Listener(EventHook { event: RenderEntityEvent.Pre ->
        if (mode.value != ESPMode.SHADER || event.entity == null || mc.renderManager.renderOutlines || !entityList.contains(event.entity)) return@EventHook
        if (hideOriginal.value) {
            // Steal it from Minecraft rendering kek
            prepareFrameBuffer()
        }
    })

    @EventHandler
    private val postRenderListener = Listener(EventHook { event: RenderEntityEvent.Post ->
        if (mode.value != ESPMode.SHADER || event.entity == null || mc.renderManager.renderOutlines || !entityList.contains(event.entity)) return@EventHook
        if (!hideOriginal.value) {
            prepareFrameBuffer()
            mc.renderManager.getEntityRenderObject<Entity>(event.entity)?.doRender(event.entity, event.x, event.y, event.z, event.yaw, event.partialTicks)
        }

        mc.framebuffer.bindFramebuffer(false)
        GlStateManager.disableOutlineMode()
        GlStateManager.popMatrix()
    })

    private fun prepareFrameBuffer() {
        GlStateManager.pushMatrix()
        GlStateManager.enableOutlineMode(0xFFFFFF)
        frameBuffer?.bindFramebuffer(false)
    }

    override fun onWorldRender(event: RenderEvent) {
        if (mc.renderManager.options == null) return
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

            ESPMode.SHADER -> {
                // Apply shader on the frame buffer
                frameBuffer?.bindFramebuffer(false)
                shader?.render(KamiTessellator.pTicks())

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
                mc.framebuffer.bindFramebuffer(false)
            }

            else -> {
                // other modes, such as GLOW, use onUpdate()
            }
        }
    }

    override fun onUpdate() {
        // Refresh frame buffer on resolution change
        refreshFrameBuffers()

        entityList.clear()
        entityList.addAll(getEntityList())

        if (mode.value == ESPMode.GLOW) {
            if (entityList.isNotEmpty()) {
                for (shader in mc.renderGlobal.entityOutlineShader.listShaders) {
                    shader.shaderManager.getShaderUniform("Radius")?.set(width.value)
                }

                for (entity in mc.world.loadedEntityList) { // Set glow for entities in the list. Remove glow for entities not in the list
                    entity.isGlowing = entityList.contains(entity)
                }
            } else {
                resetGlow()
            }
        } else if (mode.value == ESPMode.SHADER) {
            shader?.let {
                for (shader in it.listShaders) {
                    setShaderSettings(shader)
                }
            }
        }
    }

    private fun getEntityList(): List<Entity> {
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
            entityList.addAll(getTargetList(player, mob, invisible.value, range.value.toFloat()))
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
        return entityList
    }

    private fun refreshFrameBuffers() {
        if (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
            prevWidth = mc.displayWidth
            prevHeight = mc.displayHeight
            shader?.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        }
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