package org.kamiblue.client.module.modules.render

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.and
import org.kamiblue.client.util.atTrue
import org.kamiblue.client.util.atValue
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.color.DyeColors
import org.kamiblue.client.util.color.HueCycler
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.graphics.GeometryMasks
import org.kamiblue.client.util.or
import org.kamiblue.client.util.threads.safeAsyncListener
import org.kamiblue.event.listener.listener

internal object StorageESP : Module(
    name = "StorageESP",
    description = "Draws an ESP on top of storage units",
    category = Category.RENDER
) {
    private val page = setting("Page", Page.TYPE)

    /* Type settings */
    private val chest by setting("Chest", true, page.atValue(Page.TYPE))
    private val shulker by setting("Shulker", true, page.atValue(Page.TYPE))
    private val enderChest by setting("Ender Chest", true, page.atValue(Page.TYPE))
    private val frame0 = setting("Item Frame", true, page.atValue(Page.TYPE))
    private val frame by frame0
    private val withShulkerOnly by setting("With Shulker Only", true, page.atValue(Page.TYPE) and frame0.atTrue())
    private val furnace by setting("Furnace", false, page.atValue(Page.TYPE))
    private val dispenser by setting("Dispenser", false, page.atValue(Page.TYPE))
    private val hopper by setting("Hopper", false, page.atValue(Page.TYPE))
    private val cart by setting("Minecart", false, page.atValue(Page.TYPE))

    /* Color settings */
    private val colorChest by setting("Chest Color", DyeColors.ORANGE, page.atValue(Page.TYPE))
    private val colorDispenser by setting("Dispenser Color", DyeColors.LIGHT_GRAY, page.atValue(Page.TYPE))
    private val colorShulker by setting("Shulker Color", DyeColors.MAGENTA, page.atValue(Page.TYPE))
    private val colorEnderChest by setting("Ender Chest Color", DyeColors.PURPLE, page.atValue(Page.TYPE))
    private val colorFurnace by setting("Furnace Color", DyeColors.LIGHT_GRAY, page.atValue(Page.TYPE))
    private val colorHopper by setting("Hopper Color", DyeColors.GRAY, page.atValue(Page.TYPE))
    private val colorCart by setting("Cart Color", DyeColors.GREEN, page.atValue(Page.TYPE))
    private val colorFrame by setting("Frame Color", DyeColors.ORANGE, page.atValue(Page.TYPE))

    /* Render settings */
    private val filled0 = setting("Filled", true, page.atValue(Page.RENDER))
    private val filled by filled0
    private val outline0 = setting("Outline", true, page.atValue(Page.RENDER))
    private val outline by outline0
    private val tracer0 = setting("Tracer", true, page.atValue(Page.RENDER))
    private val tracer by tracer0
    private val aFilled by setting("Filled Alpha", 31, 0..255, 1, page.atValue(Page.RENDER) and filled0.atTrue())
    private val aOutline by setting("Outline Alpha", 127, 0..255, 1, page.atValue(Page.RENDER) and outline0.atTrue())
    private val aTracer by setting("Tracer Alpha", 200, 0..255, 1, page.atValue(Page.RENDER) and tracer0.atTrue())
    private val thickness by setting("Line Thickness", 2.0f, 0.25f..5.0f, 0.25f, page.atValue(Page.RENDER) and (outline0.atTrue() or tracer0.atTrue()))

    private enum class Page {
        TYPE, COLOR, RENDER
    }

    override fun getHudInfo(): String {
        return renderer.size.toString()
    }

    private var cycler = HueCycler(600)
    private val renderer = ESPRenderer()

    init {
        listener<RenderWorldEvent> {
            renderer.render(false)
        }

        safeAsyncListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeAsyncListener

            cycler++
            val cached = ArrayList<Triple<AxisAlignedBB, ColorHolder, Int>>()

            coroutineScope {
                launch(Dispatchers.Default) {
                    updateRenderer()
                }
                launch(Dispatchers.Default) {
                    updateTileEntities(cached)
                }
                launch(Dispatchers.Default) {
                    updateEntities(cached)
                }
            }

            renderer.replaceAll(cached)
        }
    }

    private fun updateRenderer() {
        renderer.aFilled = if (filled) aFilled else 0
        renderer.aOutline = if (outline) aOutline else 0
        renderer.aTracer = if (tracer) aTracer else 0
        renderer.thickness = thickness
    }

    private fun SafeClientEvent.updateTileEntities(list: MutableList<Triple<AxisAlignedBB, ColorHolder, Int>>) {
        for (tileEntity in world.loadedTileEntityList.toList()) {
            if (!checkTileEntityType(tileEntity)) continue

            val box = world.getBlockState(tileEntity.pos).getSelectedBoundingBox(world, tileEntity.pos) ?: continue
            val color = getTileEntityColor(tileEntity) ?: continue
            var side = GeometryMasks.Quad.ALL

            if (tileEntity is TileEntityChest) {
                // Leave only the colliding face and then flip the bits (~) to have ALL but that face
                if (tileEntity.adjacentChestZNeg != null) side = (side and GeometryMasks.Quad.NORTH).inv()
                if (tileEntity.adjacentChestXPos != null) side = (side and GeometryMasks.Quad.EAST).inv()
                if (tileEntity.adjacentChestZPos != null) side = (side and GeometryMasks.Quad.SOUTH).inv()
                if (tileEntity.adjacentChestXNeg != null) side = (side and GeometryMasks.Quad.WEST).inv()
            }

            synchronized(list) {
                list.add(Triple(box, color, side))
            }
        }
    }

    private fun checkTileEntityType(tileEntity: TileEntity) =
        chest && tileEntity is TileEntityChest
            || dispenser && tileEntity is TileEntityDispenser
            || shulker && tileEntity is TileEntityShulkerBox
            || enderChest && tileEntity is TileEntityEnderChest
            || furnace && tileEntity is TileEntityFurnace
            || hopper && tileEntity is TileEntityHopper

    private fun getTileEntityColor(tileEntity: TileEntity): ColorHolder? {
        val color = when (tileEntity) {
            is TileEntityChest -> colorChest
            is TileEntityDispenser -> colorDispenser
            is TileEntityShulkerBox -> colorShulker
            is TileEntityEnderChest -> colorEnderChest
            is TileEntityFurnace -> colorFurnace
            is TileEntityHopper -> colorHopper
            else -> return null
        }.color
        return if (color == DyeColors.RAINBOW.color) {
            cycler.currentRgb()
        } else color
    }

    private fun SafeClientEvent.updateEntities(list: MutableList<Triple<AxisAlignedBB, ColorHolder, Int>>) {
        for (entity in world.loadedEntityList.toList()) {
            if (!checkEntityType(entity)) continue

            val box = entity.renderBoundingBox ?: continue
            val color = getEntityColor(entity) ?: continue

            synchronized(list) {
                list.add(Triple(box, color, GeometryMasks.Quad.ALL))
            }
        }
    }

    private fun checkEntityType(entity: Entity) =
        entity is EntityItemFrame && frame && (!withShulkerOnly || entity.displayedItem.item is ItemShulkerBox)
            || (entity is EntityMinecartChest || entity is EntityMinecartHopper || entity is EntityMinecartFurnace) && cart

    private fun getEntityColor(entity: Entity): ColorHolder? {
        val color = when (entity) {
            is EntityMinecartContainer -> colorCart
            is EntityItemFrame -> colorFrame
            else -> return null
        }.color
        return if (color == DyeColors.RAINBOW.color) {
            cycler.currentRgb()
        } else color
    }

}
