package me.zeroeightsix.kami.module.modules.capes

import me.zeroeightsix.kami.module.modules.capes.Capes.Companion.getCapeResource
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Suppress("UNUSED") // Big IDE meme
class LayerCape(private val playerRenderer: RenderPlayer) : LayerRenderer<AbstractClientPlayer> {

    override fun doRenderLayer(player: AbstractClientPlayer, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float) {
        val resourceLocation = getCapeResource(player)
        val itemStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)
        if (!player.hasPlayerInfo() || player.isInvisible || !player.isWearing(EnumPlayerModelParts.CAPE) || itemStack.getItem() === Items.ELYTRA || resourceLocation == null) return

        val yOffset = if (player.isSneaking) 0.09f else 0.0f
        val zOffset = if (player.isSneaking) 0.1f else 0.14f

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        playerRenderer.bindTexture(resourceLocation)
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0f, yOffset, zOffset)

        val posX = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * partialTicks)
        val posY = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * partialTicks)
        val posZ = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks)
        val yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks

        val d3 = sin(yawOffset * 0.01745329f)
        val d4 = -cos(yawOffset * 0.01745329f)
        var f1 = posY.toFloat() * 10.0f
        f1 = MathHelper.clamp(f1, 3.0f, 32.0f)
        val f2 = max((posX * d3 + posZ * d4 * 100.0f).toFloat(), 0f)
        val f3 = (posX * d4 - posZ * d3 * 100.0f).toFloat()

        val f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks
        f1 += sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0f) * 32.0f * f4
        if (player.isSneaking) f1 += 20.0f

        GlStateManager.rotate(5.0f + f2 / 2.0f + f1, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(f3 / 2.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(-f3 / 2.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f)
        playerRenderer.mainModel.renderCape(0.0625f)
        GlStateManager.popMatrix()
    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }
}