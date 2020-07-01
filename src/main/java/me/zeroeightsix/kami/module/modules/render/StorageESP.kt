package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourUtils
import me.zeroeightsix.kami.util.GeometryMasks
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityMinecartChest
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.*
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11
import java.util.*

/**
 * Created by 086 on 10/12/2017.
 * Updated by dominikaaaa on 14/12/19
 * Updated by Afel on 08/06/20
 */
@Module.Info(
        name = "StorageESP",
        description = "Draws an ESP on top of storage units",
        category = Module.Category.RENDER
)
class StorageESP : Module() {
    private val alpha = register(Settings.integerBuilder("Alpha").withRange(1, 255).withValue(100).build())
    private val chest = register(Settings.b("Chest", true))
    private val dispenser = register(Settings.b("Dispenser", true))
    private val shulker = register(Settings.b("Shulker", true))
    private val enderChest = register(Settings.b("EnderChest", true))
    private val furnace = register(Settings.b("Furnace", true))
    private val hopper = register(Settings.b("Hopper", true))
    private val cart = register(Settings.b("Minecart", true))
    private val frame = register(Settings.b("ItemFrame", true))
    private val tracer = register(Settings.b("Tracers", true))

    private fun getTileEntityColor(tileEntity: TileEntity): Int {
        return if (tileEntity is TileEntityChest || tileEntity is TileEntityDispenser) ColourUtils.Colors.ORANGE else if (tileEntity is TileEntityShulkerBox) ColourUtils.Colors.RED else if (tileEntity is TileEntityEnderChest) ColourUtils.Colors.PURPLE else if (tileEntity is TileEntityFurnace) ColourUtils.Colors.GRAY else if (tileEntity is TileEntityHopper) ColourUtils.Colors.DARK_RED else -1
    }

    private fun getEntityColor(entity: Entity): Int {
        return if (entity is EntityMinecartChest) ColourUtils.Colors.ORANGE else if (entity is EntityItemFrame &&
                entity.displayedItem.getItem() is ItemShulkerBox) ColourUtils.Colors.YELLOW else if (entity is EntityItemFrame &&
                entity.displayedItem.getItem() !is ItemShulkerBox) ColourUtils.Colors.ORANGE else -1
    }

    override fun onWorldRender(event: RenderEvent) {
        val a = ArrayList<Triplet<BlockPos, Int, Int>>()
        GlStateManager.pushMatrix()

        for (tileEntity in Wrapper.getWorld().loadedTileEntityList) {
            val pos = tileEntity.pos
            val color = getTileEntityColor(tileEntity)
            var side = GeometryMasks.Quad.ALL
            if (tileEntity is TileEntityChest) {
                // Leave only the colliding face and then flip the bits (~) to have ALL but that face
                if (tileEntity.adjacentChestZNeg != null) side = (side and GeometryMasks.Quad.NORTH).inv()
                if (tileEntity.adjacentChestXPos != null) side = (side and GeometryMasks.Quad.EAST).inv()
                if (tileEntity.adjacentChestZPos != null) side = (side and GeometryMasks.Quad.SOUTH).inv()
                if (tileEntity.adjacentChestXNeg != null) side = (side and GeometryMasks.Quad.WEST).inv()
            }
            if (tileEntity is TileEntityChest && chest.value || tileEntity is TileEntityDispenser && dispenser.value || tileEntity is TileEntityShulkerBox && shulker.value || tileEntity is TileEntityEnderChest && enderChest.value || tileEntity is TileEntityFurnace && furnace.value || tileEntity is TileEntityHopper && hopper.value) if (color != -1) a.add(Triplet(pos, color, side)) //GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, color);
        }

        for (entity in Wrapper.getWorld().loadedEntityList) {
            val pos = entity.position
            val color = getEntityColor(entity)
            if (entity is EntityItemFrame && frame.value || entity is EntityMinecartChest && cart.value) if (color != -1) a.add(Triplet(if (entity is EntityItemFrame) pos.add(0, -1, 0) else pos, color, GeometryMasks.Quad.ALL)) //GeometryTessellator.drawCuboid(event.getBuffer(), entity instanceof EntityItemFrame ? pos.add(0, -1, 0) : pos, GeometryMasks.Line.ALL, color);
        }

        KamiTessellator.prepare(GL11.GL_QUADS) //pair.first = pos, pair.second = color

        for (pair in a) {
            KamiTessellator.drawBox(pair.first, changeAlpha(pair.second, alpha.value), pair.third)
        }

        KamiTessellator.release()
        GlStateManager.popMatrix()
        GlStateManager.enableTexture2D()

        if (tracer.value) {
            for (pair in a) {
                KamiTessellator.drawLineToBlock(pair.first, pair.second, (alpha.value.toFloat()) / 255)
            }
        }

    }

    private fun changeAlpha(origColor: Int, userInputedAlpha: Int): Int {
        var origColor = origColor
        origColor = origColor and 0x00ffffff //drop the previous alpha value
        return userInputedAlpha shl 24 or origColor //add the one the user inputted
    }

    inner class Triplet<T, U, V>(val first: T, val second: U, val third: V)
}
