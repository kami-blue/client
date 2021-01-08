package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import me.zeroeightsix.kami.util.color.HueCycler
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import me.zeroeightsix.kami.util.threads.safeAsyncListener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

object StorageESP : Module(
    name = KamiLang.get("module.modules.render.StorageESP.Storageesp"),
    description = KamiLang.get("module.modules.render.StorageESP.DrawsAnEspOn"),
    category = Category.RENDER
) {
    private val page by setting(KamiLang.get("module.modules.render.StorageESP.Page"), Page.TYPE)

    /* Type settings */
    private val chest by setting(KamiLang.get("module.modules.render.StorageESP.Chest"), true, { page == Page.TYPE })
    private val shulker by setting(KamiLang.get("module.modules.render.StorageESP.Shulker"), true, { page == Page.TYPE })
    private val enderChest by setting(KamiLang.get("module.modules.render.StorageESP.Enderchest"), true, { page == Page.TYPE })
    private val frame by setting(KamiLang.get("module.modules.render.StorageESP.Itemframe"), true, { page == Page.TYPE })
    private val withShulkerOnly by setting(KamiLang.get("module.modules.render.StorageESP.Withshulkeronly"), true, { page == Page.TYPE && frame })
    private val furnace by setting(KamiLang.get("module.modules.render.StorageESP.Furnace"), false, { page == Page.TYPE })
    private val dispenser by setting(KamiLang.get("module.modules.render.StorageESP.Dispenser"), false, { page == Page.TYPE })
    private val hopper by setting(KamiLang.get("module.modules.render.StorageESP.Hopper"), false, { page == Page.TYPE })
    private val cart by setting(KamiLang.get("module.modules.render.StorageESP.Minecart"), false, { page == Page.TYPE })

    /* Color settings */
    private val colorChest by setting(KamiLang.get("module.modules.render.StorageESP.Chestcolor"), DyeColors.ORANGE, { page == Page.COLOR })
    private val colorDispenser by setting(KamiLang.get("module.modules.render.StorageESP.Dispensercolor"), DyeColors.LIGHT_GRAY, { page == Page.COLOR })
    private val colorShulker by setting(KamiLang.get("module.modules.render.StorageESP.Shulkercolor"), DyeColors.MAGENTA, { page == Page.COLOR })
    private val colorEnderChest by setting(KamiLang.get("module.modules.render.StorageESP.Enderchestcolor"), DyeColors.PURPLE, { page == Page.COLOR })
    private val colorFurnace by setting(KamiLang.get("module.modules.render.StorageESP.Furnacecolor"), DyeColors.LIGHT_GRAY, { page == Page.COLOR })
    private val colorHopper by setting(KamiLang.get("module.modules.render.StorageESP.Hoppercolor"), DyeColors.GRAY, { page == Page.COLOR })
    private val colorCart by setting(KamiLang.get("module.modules.render.StorageESP.Cartcolor"), DyeColors.GREEN, { page == Page.COLOR })
    private val colorFrame by setting(KamiLang.get("module.modules.render.StorageESP.Framecolor"), DyeColors.ORANGE, { page == Page.COLOR })

    /* Render settings */
    private val filled by setting(KamiLang.get("module.modules.render.StorageESP.Filled"), true, { page == Page.RENDER })
    private val outline by setting(KamiLang.get("module.modules.render.StorageESP.Outline"), true, { page == Page.RENDER })
    private val tracer by setting(KamiLang.get("module.modules.render.StorageESP.Tracer"), false, { page == Page.RENDER })
    private val aFilled by setting(KamiLang.get("module.modules.render.StorageESP.Filledalpha"), 31, 0..255, 1, { page == Page.RENDER && filled })
    private val aOutline by setting(KamiLang.get("module.modules.render.StorageESP.Outlinealpha"), 127, 0..255, 1, { page == Page.RENDER && outline })
    private val aTracer by setting(KamiLang.get("module.modules.render.StorageESP.Traceralpha"), 200, 0..255, 1, { page == Page.RENDER && tracer })
    private val thickness by setting(KamiLang.get("module.modules.render.StorageESP.Linethickness"), 2.0f, 0.25f..5.0f, 0.25f, { page == Page.RENDER })

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
            renderer.clear()
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

            list.add(Triple(box, color, side))
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

            list.add(Triple(box, color, GeometryMasks.Quad.ALL))
        }
    }

    private fun checkEntityType(entity: Entity) =
        entity is EntityItemFrame && frameShulkerOrAny(entity)
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

    private fun frameShulkerOrAny(entity: EntityItemFrame) =
        frame && (!withShulkerOnly || entity.displayedItem.item is ItemShulkerBox)
}
