package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import me.zeroeightsix.kami.util.color.HueCycler
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.concurrent.ConcurrentHashMap

object StorageESP : Module(
    name = "StorageESP",
    description = "Draws an ESP on top of storage units",
    category = Category.RENDER
) {
    private val page = setting("Page", Page.TYPE)

    /* Type settings */
    private val chest = setting("Chest", true, { page.value == Page.TYPE })
    private val shulker = setting("Shulker", true, { page.value == Page.TYPE })
    private val enderChest = setting("EnderChest", true, { page.value == Page.TYPE })
    private val frame = setting("ItemFrame", true, { page.value == Page.TYPE })
    private val frameShulker = setting("ItFShulkerOnly", true, { frame.value && page.value == Page.TYPE })
    private val furnace = setting("Furnace", false, { page.value == Page.TYPE })
    private val dispenser = setting("Dispenser", false, { page.value == Page.TYPE })
    private val hopper = setting("Hopper", false, { page.value == Page.TYPE })
    private val cart = setting("Minecart", false, { page.value == Page.TYPE })

    /* Color settings */
    private val colorChest = setting("ChestColor", DyeColors.ORANGE, { page.value == Page.COLOR })
    private val colorDispenser = setting("DispenserColor", DyeColors.LIGHT_GRAY, { page.value == Page.COLOR })
    private val colorShulker = setting("ShulkerColor", DyeColors.MAGENTA, { page.value == Page.COLOR })
    private val colorEnderChest = setting("EnderChestColor", DyeColors.PURPLE, { page.value == Page.COLOR })
    private val colorFurnace = setting("FurnaceColor", DyeColors.LIGHT_GRAY, { page.value == Page.COLOR })
    private val colorHopper = setting("HopperColor", DyeColors.GRAY, { page.value == Page.COLOR })
    private val colorCart = setting("CartColor", DyeColors.GREEN, { page.value == Page.COLOR })
    private val colorFrame = setting("FrameColor", DyeColors.ORANGE, { page.value == Page.COLOR })

    /* Render settings */
    private val filled = setting("Filled", true, { page.value == Page.RENDER })
    private val outline = setting("Outline", true, { page.value == Page.RENDER })
    private val tracer = setting("Tracer", false, { page.value == Page.RENDER })
    private val cull = setting("Culling", true, { page.value == Page.RENDER })
    private val aFilled = setting("FilledAlpha", 31, 0..255, 1, { page.value == Page.RENDER && filled.value })
    private val aOutline = setting("OutlineAlpha", 127, 0..255, 1, { page.value == Page.RENDER && outline.value })
    private val aTracer = setting("TracerAlpha", 200, 0..255, 1, { page.value == Page.RENDER && tracer.value })
    private val thickness = setting("LineThickness", 2.0f, 0.25f..5.0f, 0.25f, { page.value == Page.RENDER })

    private enum class Page {
        TYPE, COLOR, RENDER
    }

    private val renderList = ConcurrentHashMap<AxisAlignedBB, Pair<ColorHolder, Int>>()
    private var cycler = HueCycler(600)

    init {
        listener<RenderWorldEvent> {
            val renderer = ESPRenderer()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            renderer.aTracer = if (tracer.value) aTracer.value else 0
            renderer.thickness = thickness.value
            for ((box, pair) in renderList) {
                renderer.add(box, pair.first, pair.second)
            }
            renderer.render(true, cull.value)
        }

        safeListener<TickEvent.ClientTickEvent> {
            cycler++
            renderList.clear()
            for (tileEntity in world.loadedTileEntityList) {
                if (tileEntity is TileEntityChest && chest.value
                        || tileEntity is TileEntityDispenser && dispenser.value
                        || tileEntity is TileEntityShulkerBox && shulker.value
                        || tileEntity is TileEntityEnderChest && enderChest.value
                        || tileEntity is TileEntityFurnace && furnace.value
                        || tileEntity is TileEntityHopper && hopper.value) {
                    val box = world.getBlockState(tileEntity.pos).getSelectedBoundingBox(world, tileEntity.pos)
                    val color = getTileEntityColor(tileEntity) ?: continue
                    var side = GeometryMasks.Quad.ALL
                    if (tileEntity is TileEntityChest) {
                        // Leave only the colliding face and then flip the bits (~) to have ALL but that face
                        if (tileEntity.adjacentChestZNeg != null) side = (side and GeometryMasks.Quad.NORTH).inv()
                        if (tileEntity.adjacentChestXPos != null) side = (side and GeometryMasks.Quad.EAST).inv()
                        if (tileEntity.adjacentChestZPos != null) side = (side and GeometryMasks.Quad.SOUTH).inv()
                        if (tileEntity.adjacentChestXNeg != null) side = (side and GeometryMasks.Quad.WEST).inv()
                    }
                    renderList[box] = Pair(color, side)
                }
            }

            for (entity in world.loadedEntityList) {
                if (entity is EntityItemFrame && frameShulkerOrAny(entity)
                        || (entity is EntityMinecartChest
                                || entity is EntityMinecartHopper
                                || entity is EntityMinecartFurnace) && cart.value) {
                    val box = entity.renderBoundingBox
                    val color = getEntityColor(entity) ?: continue
                    renderList[box] = Pair(color, GeometryMasks.Quad.ALL)
                }
            }
        }
    }

    private fun getTileEntityColor(tileEntity: TileEntity): ColorHolder? {
        val color = when (tileEntity) {
            is TileEntityChest -> colorChest
            is TileEntityDispenser -> colorDispenser
            is TileEntityShulkerBox -> colorShulker
            is TileEntityEnderChest -> colorEnderChest
            is TileEntityFurnace -> colorFurnace
            is TileEntityHopper -> colorHopper
            else -> return null
        }.value.color
        return if (color == DyeColors.RAINBOW.color) {
            cycler.currentRgb()
        } else color
    }

    private fun getEntityColor(entity: Entity): ColorHolder? {
        val color = when (entity) {
            is EntityMinecartContainer -> colorCart
            is EntityItemFrame -> colorFrame
            else -> return null
        }.value.color
        return if (color == DyeColors.RAINBOW.color) {
            cycler.currentRgb()
        } else color
    }

    private fun frameShulkerOrAny(e: EntityItemFrame): Boolean {
        return when {
            !frame.value -> false
            !frameShulker.value -> true
            else -> e.displayedItem.getItem() is ItemShulkerBox
        }
    }
}
