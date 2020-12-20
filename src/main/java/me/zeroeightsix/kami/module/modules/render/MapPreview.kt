package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.mixin.client.gui.MixinGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraft.world.storage.MapData

/**
 * @see MixinGuiScreen.renderToolTip
 */
@Module.Info(
        name = "MapPreview",
        category = Module.Category.RENDER,
        description = "Previews maps when hovering over them"
)
object MapPreview : Module() {
    val frame = register(Settings.b("ShowFrame", true))
    val scale = register(Settings.doubleBuilder("Size").withRange(0.0, 10.0).withValue(5.0).build())

    @JvmStatic
    fun getMapData(itemStack: ItemStack): MapData? {
        return (itemStack.getItem() as ItemMap).getMapData(itemStack, mc.world as World)
    }
}