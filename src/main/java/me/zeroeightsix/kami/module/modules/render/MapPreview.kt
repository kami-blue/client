package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraft.world.storage.MapData


/**
 * Created by fred41 on 27/05/2020.
 * Greatly updated by dominikaaaa on 01/06/20, fixed loads of bugs
 * @see me.zeroeightsix.kami.mixin.client.MixinGuiScreen
 */
@Module.Info(
        name = "MapPreview",
        category = Module.Category.RENDER,
        description = "Previews maps when hovering over them"
)
class MapPreview : Module() {
    val frame: Setting<Boolean> = register(Settings.b("Show Frame", true))
    val scale: Setting<Double> = register(Settings.doubleBuilder("Size").withRange(0.0, 10.0).withValue(5.0).build())

    companion object MapPreview {
        @JvmStatic
        fun getMapData(itemStack: ItemStack): MapData? {
            return (itemStack.getItem() as ItemMap).getMapData(itemStack, mc.world as World)
        }
    }
}