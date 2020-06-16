package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

/**
 * Created by 086 on 11/12/2017.
 * Kurisu Makise is best girl
 * Updated by Afel on 08/06/20
 */
@Module.Info(
        name = "Tracers",
        description = "Draws lines to other living entities",
        category = Module.Category.RENDER
)
class Tracers : Module() {
    private val players = register(Settings.b("Players", true))
    private val friends = register(Settings.b("Friends", true))
    private val mobs = register(Settings.b("Mobs", false))
    private val passive = register(Settings.booleanBuilder("Passive Mobs").withValue(false).withVisibility { mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("Neutral Mobs").withValue(true).withVisibility { mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("Hostile Mobs").withValue(true).withVisibility { mobs.value }.build())
    private val range = register(Settings.d("Range", 200.0))
    private val renderInvis = register(Settings.b("Invisible", false))
    private val customColours = register(Settings.booleanBuilder("Custom Colours").withValue(true).build())
    private val opacity = register(Settings.floatBuilder("Opacity").withRange(0f, 1f).withValue(1f).build())
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { customColours.value }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { customColours.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { customColours.value }.build())
    private var cycler = HueCycler(3600)

    override fun onWorldRender(event: RenderEvent) {
        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().world.loadedEntityList.stream()
                .filter { e: Entity? -> EntityUtil.isLiving(e) }
                .filter { entity: Entity ->
                    if (entity.isInvisible) {
                        return@filter renderInvis.value
                    }
                    true
                }
                .filter { entity: Entity? -> !EntityUtil.isFakeLocalPlayer(entity) }
                .filter { entity: Entity -> if (entity is EntityPlayer) players.value && mc.player !== entity else EntityUtil.mobTypeSettings(entity, mobs.value, passive.value, neutral.value, hostile.value) }
                .filter { entity: Entity? -> mc.player.getDistance(entity) < range.value }
                .forEach { entity: Entity ->
                    var colour = getColour(entity)

                    colour = if (colour == ColourUtils.Colors.RAINBOW) {
                        if (!friends.value) return@forEach
                        if (customColours.value) {
                            ColourConverter.rgbToInt(r.value, g.value, b.value, (opacity.value * 255f).toInt())
                        } else {
                            cycler.current()
                        }
                    } else {
                        cycler.current()
                    }

                    KamiTessellator.drawLineToEntity(entity, colour, opacity.value, mc.renderPartialTicks)
                }
        GlStateManager.popMatrix()
    }

    override fun onUpdate() {
        cycler.next()
    }

    private fun getColour(entity: Entity): Int {
        return if (entity is EntityPlayer) {
            if (Friends.isFriend(entity.getName())) ColourUtils.Colors.RAINBOW else ColourUtils.Colors.WHITE
        } else {
            if (EntityUtil.isPassiveMob(entity)) ColourUtils.Colors.GREEN else if (EntityUtil.isCurrentlyNeutral(entity)) ColourUtils.Colors.BLUE else ColourUtils.Colors.RED
        }
    }
}