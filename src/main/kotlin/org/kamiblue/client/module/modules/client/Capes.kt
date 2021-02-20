package org.kamiblue.client.module.modules.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelElytra
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderLivingBase
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerArmorBase
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.kamiblue.capeapi.Cape
import org.kamiblue.capeapi.CapeType
import org.kamiblue.capeapi.CapeUser
import org.kamiblue.client.KamiMod
import org.kamiblue.client.gui.hudgui.elements.client.WaterMark
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.misc.DiscordRPC
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.color.ColorConverter
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.color.DyeColors
import org.kamiblue.client.util.threads.BackgroundScope
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.commons.extension.synchronized
import org.kamiblue.commons.utils.ConnectionUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.cos
import kotlin.math.sin

internal object Capes : Module(
    name = "Capes",
    category = Category.CLIENT,
    description = "Controls the display of KAMI Blue capes",
    showOnArray = false,
    enabledByDefault = true
) {
    private val capeUsers = HashMap<UUID, Cape>().synchronized()

    var updated = false; private set
    var isPremium = false; private set

    private val gson = Gson()
    private val type = TypeToken.getArray(CapeUser::class.java).type

    init {
        onEnable {
            defaultScope.launch {
                updateCapes()
            }
        }

        BackgroundScope.launchLooping("Cape", 300000L) {
            updateCapes()
        }
    }

    private suspend fun updateCapes() {
        val rawJson = withContext(Dispatchers.IO) {
            ConnectionUtils.requestRawJsonFrom(KamiMod.CAPES_JSON) {
                KamiMod.LOG.warn("Failed requesting capes", it)
            }
        } ?: return

        try {
            var capeType: CapeType? = null
            val cacheList = gson.fromJson<Array<CapeUser>>(rawJson, type)
            capeUsers.clear()

            cacheList.forEach { capeUser ->
                capeUser.capes.forEach { cape ->
                    cape.playerUUID?.let {
                        capeUsers[it] = cape
                        if (it == mc.session.profile.id) { // if any of the capeUser's capes match current UUID
                            isPremium = isPremium || capeUser.isPremium // || is to prevent bug if there is somehow a duplicate capeUser
                            capeType = cape.type
                        }
                    }
                }
            }

            updated = true
            WaterMark.visible = WaterMark.visible
            DiscordRPC.setCustomIcons(capeType)
            KamiMod.LOG.info("Capes loaded")
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed parsing capes", e)
        }
    }

    fun tryRenderCape(playerRenderer: RenderPlayer, player: AbstractClientPlayer, partialTicks: Float): Boolean {
        if (isDisabled
            || !player.hasPlayerInfo()
            || player.isInvisible
            || !player.isWearing(EnumPlayerModelParts.CAPE)
            || player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).item == Items.ELYTRA) return false

        val cape = capeUsers[player.gameProfile.id]

        return if (cape != null) {
            renderCape(playerRenderer, player, partialTicks, cape)
        } else {
            false
        }
    }

    private fun renderCape(playerRenderer: RenderPlayer, player: AbstractClientPlayer, partialTicks: Float, cape: Cape): Boolean {
        val primaryColor = parseColor(cape.color.primary)
        val borderColor = parseColor(cape.color.border)

        if (primaryColor == null || borderColor == null) return false

        renderCapeLayer(playerRenderer, player, CapeTexture.PRIMARY, primaryColor, partialTicks)
        renderCapeLayer(playerRenderer, player, CapeTexture.BORDER, borderColor, partialTicks)

        if (cape.type == CapeType.CONTRIBUTOR) {
            renderCapeLayer(playerRenderer, player, CapeTexture.TEXT_ICON, DyeColors.WHITE.color, partialTicks)
        } else {
            renderCapeLayer(playerRenderer, player, CapeTexture.TEXT, DyeColors.WHITE.color, partialTicks)
        }

        return true
    }

    private fun renderCapeLayer(renderer: RenderPlayer, player: AbstractClientPlayer, texture: CapeTexture, color: ColorHolder, partialTicks: Float) {
        GlStateManager.color(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 1.0f)
        renderer.bindTexture(texture.location)
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0f, 0.0f, 0.125f)

        val interpolatedPos = EntityUtils.getInterpolatedPos(player, partialTicks)
        val relativePosX = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks - interpolatedPos.x
        val relativePosY = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks - interpolatedPos.y
        val relativePosZ = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks - interpolatedPos.z

        val yawOffset = Math.toRadians(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks.toDouble())
        val relativeX = sin(yawOffset)
        val relativeZ = -cos(yawOffset)

        var angle1 = (relativePosY.toFloat() * 10.0f).coerceIn(-6.0f, 32.0f)

        var angle2 = (relativePosX * relativeX + relativePosZ * relativeZ).toFloat() * 100.0f
        val angle3 = (relativePosX * relativeZ - relativePosZ * relativeX).toFloat() * 100.0f
        if (angle2 < 0.0f) {
            angle2 = 0.0f
        }

        val cameraYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks
        val walkedDist = player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks
        angle1 += sin((walkedDist) * 6.0f) * 32.0f * cameraYaw
        if (player.isSneaking) {
            angle1 += 25.0f
        }

        GlStateManager.rotate(6.0f + angle2 / 2.0f + angle1, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(angle3 / 2.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(-angle3 / 2.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f)

        renderer.mainModel.renderCape(0.0625f)
        GlStateManager.popMatrix()
    }

    fun tryRenderElytra(
        renderer: RenderLivingBase<*>,
        model: ModelElytra,
        entity: EntityLivingBase,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        scale: Float,
        partialTicks: Float
    ): Boolean {
        if (isDisabled
            || entity !is AbstractClientPlayer
            || entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).item != Items.ELYTRA) return false

        val cape = capeUsers[entity.gameProfile.id]

        return if (cape != null) {
            renderElytra(renderer, model, entity, cape, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, partialTicks)
        } else {
            false
        }
    }

    private fun renderElytra(
        renderer: RenderLivingBase<*>,
        model: ModelElytra,
        player: AbstractClientPlayer,
        cape: Cape,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        scale: Float,
        partialTicks: Float
    ): Boolean {
        val primaryColor = parseColor(cape.color.primary)
        val borderColor = parseColor(cape.color.border)

        if (primaryColor == null || borderColor == null) return false

        renderElytraLayer(renderer, model, player, CapeTexture.PRIMARY, primaryColor, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, partialTicks)
        renderElytraLayer(renderer, model, player, CapeTexture.BORDER, borderColor, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, partialTicks)

        if (cape.type == CapeType.CONTRIBUTOR) {
            renderElytraLayer(renderer, model, player, CapeTexture.TEXT_ICON, DyeColors.WHITE.color, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, partialTicks)
        } else {
            renderElytraLayer(renderer, model, player, CapeTexture.TEXT, DyeColors.WHITE.color, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, partialTicks)
        }

        return true
    }

    private fun renderElytraLayer(
        renderer: RenderLivingBase<*>,
        model: ModelElytra,
        player: AbstractClientPlayer,
        texture: CapeTexture,
        color: ColorHolder,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        scale: Float,
        partialTicks: Float
    ) {
        GlStateManager.color(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 1.0f)
        renderer.bindTexture(texture.location)
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0f, 0.0f, 0.125f)
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player)
        model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)

        if (player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isItemEnchanted) {
            LayerArmorBase.renderEnchantedGlint(renderer, player, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale)
        }

        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    private fun parseColor(string: String) = string.toIntOrNull(16)?.let {
        ColorConverter.hexToRgb(it)
    }

    private enum class CapeTexture(val location: ResourceLocation) {
        PRIMARY(ResourceLocation("kamiblue/textures/capes/primary.png")),
        BORDER(ResourceLocation("kamiblue/textures/capes/border.png")),
        TEXT(ResourceLocation("kamiblue/textures/capes/text.png")),
        TEXT_ICON(ResourceLocation("kamiblue/textures/capes/text_icon.png"))
    }

}