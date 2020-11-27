package me.zeroeightsix.kami.module.modules.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.DiscordRPC
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import me.zeroeightsix.kami.util.event.listener
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
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.capeapi.Cape
import org.kamiblue.capeapi.CapeType
import org.kamiblue.capeapi.CapeUser
import org.kamiblue.commons.utils.ConnectionUtils
import org.kamiblue.commons.utils.ThreadUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(
        name = "Capes",
        category = Module.Category.CLIENT,
        description = "Controls the display of KAMI Blue capes",
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
object Capes : Module() {
    private val capeUsers = Collections.synchronizedMap(HashMap<UUID, Cape>())
    var isPremium = false; private set

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)
    private val gson = Gson()
    private val thread = Thread { updateCapes() }

    override fun onEnable() {
        ThreadUtils.submitTask(thread)
    }

    init {
        listener<TickEvent.ClientTickEvent> {
            if (timer.tick(5L)) ThreadUtils.submitTask(thread)
        }
    }

    private fun updateCapes() {
        val rawJson = ConnectionUtils.requestRawJsonFrom(KamiMod.CAPES_JSON) {
            KamiMod.LOG.warn("Failed requesting capes", it)
        } ?: return

        try {
            var type: CapeType? = null
            val cacheList = gson.fromJson<ArrayList<CapeUser>>(rawJson, object : TypeToken<List<CapeUser>>() {}.type)
            capeUsers.clear()

            cacheList.forEach { capeUser ->
                capeUser.capes.forEach { cape ->
                    cape.playerUUID?.let {
                        capeUsers[it] = cape
                        if (it == mc.session.profile.id) { // if any of the capeUser's capes match current UUID
                            isPremium = isPremium || capeUser.isPremium // || is to prevent bug if there is somehow a duplicate capeUser
                            type = cape.type
                        }
                    }
                }
            }

            DiscordRPC.setCustomIcons(type)
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
                || player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) return false

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

        var angle1 = MathHelper.clamp(relativePosY.toFloat() * 10.0f, -6.0f, 32.0f)

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
                || entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) return false

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