package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourConverter.rgbToInt
import me.zeroeightsix.kami.util.ColourHolder
import me.zeroeightsix.kami.util.ColourUtils
import me.zeroeightsix.kami.util.ESPRenderer
import me.zeroeightsix.kami.util.GeometryMasks
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityMinecartChest
import net.minecraft.entity.item.EntityMinecartFurnace
import net.minecraft.entity.item.EntityMinecartHopper
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.*
import net.minecraft.util.math.AxisAlignedBB
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by 086 on 10/12/2017.
 * Updated by dominikaaaa on 14/12/19
 * Updated by Afel on 08/06/20
 * Updated by Xiaro on 23/07/20
 */
@Module.Info(
        name = "StorageESP",
        description = "Draws an ESP on top of storage units",
        category = Module.Category.RENDER
)
class StorageESP : Module() {
    private val chest = register(Settings.b("Chest", true))
    private val dispenser = register(Settings.b("Dispenser", true))
    private val shulker = register(Settings.b("Shulker", true))
    private val enderChest = register(Settings.b("EnderChest", true))
    private val furnace = register(Settings.b("Furnace", true))
    private val hopper = register(Settings.b("Hopper", true))
    private val cart = register(Settings.b("Minecart", true))
    private val frame = register(Settings.b("ItemFrame", true))
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val tracer = register(Settings.b("Tracer", true))
    private val customColours = register(Settings.b("CustomColours", false))
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { customColours.value }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { customColours.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { customColours.value }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(31).withRange(0, 255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(127).withRange(0, 255).withVisibility { outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(200).withRange(0, 255).withVisibility { tracer.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).build())

    private val renderList = ConcurrentHashMap<AxisAlignedBB, Pair<ColourHolder, Int>>()

    override fun onWorldRender(event: RenderEvent) {
        val renderer = ESPRenderer(event.partialTicks)
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        renderer.aTracer = if (tracer.value) aTracer.value else 0
        renderer.thickness = thickness.value
        for ((box, pair) in renderList) {
            renderer.add(box, pair.first, pair.second)
        }
        renderer.render()
    }

    override fun onUpdate() {
        renderList.clear()
        for (tileEntity in mc.world.loadedTileEntityList) {
            if (tileEntity is TileEntityChest && chest.value
                    || tileEntity is TileEntityDispenser && dispenser.value
                    || tileEntity is TileEntityShulkerBox && shulker.value
                    || tileEntity is TileEntityEnderChest && enderChest.value
                    || tileEntity is TileEntityFurnace && furnace.value
                    || tileEntity is TileEntityHopper && hopper.value) {
                val box = mc.world.getBlockState(tileEntity.pos).getSelectedBoundingBox(mc.world, tileEntity.pos)
                val rgb = getTileEntityColor(tileEntity)
                var side = GeometryMasks.Quad.ALL
                if (tileEntity is TileEntityChest) {
                    // Leave only the colliding face and then flip the bits (~) to have ALL but that face
                    if (tileEntity.adjacentChestZNeg != null) side = (side and GeometryMasks.Quad.NORTH).inv()
                    if (tileEntity.adjacentChestXPos != null) side = (side and GeometryMasks.Quad.EAST).inv()
                    if (tileEntity.adjacentChestZPos != null) side = (side and GeometryMasks.Quad.SOUTH).inv()
                    if (tileEntity.adjacentChestXNeg != null) side = (side and GeometryMasks.Quad.WEST).inv()
                }
                if (rgb != -1) {
                    val rgba = ColourHolder((rgb shr 16), (rgb shr 8 and 255), (rgb and 255))
                    renderList[box] = Pair(rgba, side)
                }
            }
        }

        for (entity in mc.world.loadedEntityList) {
            if (entity is EntityItemFrame && frame.value
                    || (entity is EntityMinecartChest
                            || entity is EntityMinecartHopper
                            || entity is EntityMinecartFurnace) && cart.value) {
                val box = entity.renderBoundingBox
                val rgb = getEntityColor(entity)
                if (rgb != -1) {
                    val rgba = ColourHolder((rgb shr 16), (rgb shr 8 and 255), (rgb and 255))
                    renderList[box] = Pair(rgba, GeometryMasks.Quad.ALL)
                }
            }
        }
    }

    private fun getTileEntityColor(tileEntity: TileEntity): Int {
        return if (customColours.value) rgbToInt(r.value, g.value, b.value) else when (tileEntity) {
            is TileEntityChest -> ColourUtils.Colors.ORANGE
            is TileEntityDispenser -> ColourUtils.Colors.ORANGE
            is TileEntityShulkerBox -> ColourUtils.Colors.RED
            is TileEntityEnderChest -> ColourUtils.Colors.PURPLE
            is TileEntityFurnace -> ColourUtils.Colors.GRAY
            is TileEntityHopper -> ColourUtils.Colors.DARK_RED
            else -> -1
        }
    }

    private fun getEntityColor(entity: Entity): Int {
        return if (customColours.value) rgbToInt(r.value, g.value, b.value) else when {
            entity is EntityMinecartChest -> ColourUtils.Colors.ORANGE
            entity is EntityMinecartHopper -> ColourUtils.Colors.DARK_RED
            entity is EntityItemFrame && entity.displayedItem.getItem() is ItemShulkerBox -> ColourUtils.Colors.YELLOW
            entity is EntityItemFrame && entity.displayedItem.getItem() !is ItemShulkerBox -> ColourUtils.Colors.ORANGE
            else -> -1
        }
    }
}
