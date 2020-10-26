package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraft.world.storage.MapData


/**
 * @see me.zeroeightsix.kami.mixin.client.MixinGuiScreen
 */
@Module.Info(
        name = "MapPreview",
        category = Module.Category.RENDER,
        description = "Previews maps when hovering over them"
)
object MapPreview : Module() {
    val frame = setting("ShowFrame", true)
    val scale = setting("Size", 5.0f, 0.0f..10.0f, 0.5f)

    @JvmStatic
    fun getMapData(itemStack: ItemStack): MapData? {
        return (itemStack.getItem() as ItemMap).getMapData(itemStack, mc.world as World)
    }
}