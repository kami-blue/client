package me.zeroeightsix.kami.module.modules.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.kamiblue.capeapi.Cape
import org.kamiblue.capeapi.CapeType
import org.kamiblue.capeapi.CapeUser
import org.kamiblue.commons.utils.ThreadUtils
import java.io.File
import java.io.FileReader
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
    private val capeUsers: MutableMap<UUID, Cape> = Collections.synchronizedMap(HashMap<UUID, Cape>())
    var isPremium = false; private set

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)
    private val file = File(KamiMod.DIRECTORY + "capes.json")
    private val gson = Gson()
    private val thread = Thread({ updateCapes() }, "Capes Update Thread")

    override fun onEnable() {
        ThreadUtils.submitTask(thread)
    }

    init {
        listener<SafeTickEvent> {
            if (timer.tick(5L)) ThreadUtils.submitTask(thread)
        }
    }

    private fun updateCapes() {
        /*val rawJson = ConnectionUtils.requestRawJsonFrom(KamiMod.CAPES_JSON) {
            KamiMod.log.warn("Failed requesting capes", it)
        } ?: return

        try {
            val cacheList = gson.fromJson<ArrayList<CapeUser>>(rawJson, object : TypeToken<List<CapeUser>>() {}.type)
            cacheList.forEach { capeUser ->
                capeUser.capes.forEach { cape ->
                    cape.playerUUID?.let {
                        capeUsers[it] = cape
                        isPremium = isPremium || mc.session.profile.id == it && capeUser.isPremium
                    }
                }
            }
            KamiMod.log.info("Capes loaded")
        } catch (e: Exception) {
            KamiMod.log.warn("Failed parsing capes", e)
        }*/

        val reader = FileReader(file)
        try {
            val cacheList = gson.fromJson<ArrayList<CapeUser>>(reader, object : TypeToken<List<CapeUser>>() {}.type)
            capeUsers.clear()
            cacheList.forEach { capeUser ->
                capeUser.capes.forEach { cape ->
                    cape.playerUUID?.let {
                        capeUsers[it] = cape
                        isPremium = isPremium || mc.session.profile.id == it && capeUser.isPremium
                    }
                }
            }
            KamiMod.log.info("Capes loaded")
        } catch (e: Exception) {
            KamiMod.log.warn("Failed parsing capes", e)
        }
        println(capeUsers)
    }

    fun tryRenderCape(playerRenderer: RenderPlayer, player: AbstractClientPlayer, partialTicks: Float): Boolean {
        if (!player.hasPlayerInfo()
                || player.isInvisible
                || !player.isWearing(EnumPlayerModelParts.CAPE)
                || player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) return false

        val cape = capeUsers[player.gameProfile.id]

        return if (cape != null) {
            renderCape(playerRenderer, player, partialTicks, cape)
            true
        } else {
            false
        }
    }

    private fun renderCape(playerRenderer: RenderPlayer, player: AbstractClientPlayer, partialTicks: Float, cape: Cape) {
        val primaryColor = cape.color.primary.toIntOrNull(16)?.let {
            ColorConverter.hexToRgb(it)
        } ?: return
        val borderColor = cape.color.border.toIntOrNull(16)?.let {
            ColorConverter.hexToRgb(it)
        } ?: return

        renderCapeLayer(playerRenderer, player, CapeTexture.PRIMARY, primaryColor, partialTicks)
        renderCapeLayer(playerRenderer, player, CapeTexture.BORDER, borderColor, partialTicks)
        renderCapeLayer(playerRenderer, player, CapeTexture.TEXT, DyeColors.WHITE.color, partialTicks)

        if (cape.type == CapeType.CONTRIBUTOR) {
            renderCapeLayer(playerRenderer, player, CapeTexture.ICON, DyeColors.WHITE.color, partialTicks)
        }
    }

    private fun renderCapeLayer(playerRenderer: RenderPlayer, player: AbstractClientPlayer, texture: CapeTexture, color: ColorHolder, partialTicks: Float) {
        GlStateManager.color(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 1.0f)
        playerRenderer.bindTexture(texture.location)
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

        playerRenderer.mainModel.renderCape(0.0625f)
        GlStateManager.popMatrix()
    }

    private enum class CapeTexture(val location: ResourceLocation) {
        BORDER(ResourceLocation("kamiblue/textures/capes/border.png")),
        PRIMARY(ResourceLocation("kamiblue/textures/capes/primary.png")),
        TEXT(ResourceLocation("kamiblue/textures/capes/text.png")),
        ICON(ResourceLocation("kamiblue/textures/capes/icon.png"))
    }

}