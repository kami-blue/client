package org.kamiblue.client.module.modules.render

import net.minecraft.block.state.IBlockState
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.setting.settings.impl.collection.CollectionSetting

internal object Xray : Module(
    name = "Xray",
    description = "Lets you see through blocks",
    category = Category.RENDER
) {
    private val defaultVisibleList = linkedSetOf("minecraft:diamond_ore", "minecraft:iron_ore", "minecraft:gold_ore", "minecraft:portal", "minecraft:cobblestone")

    val visibleList = setting(CollectionSetting("Visible List", defaultVisibleList, { false }))

    @JvmStatic
    fun shouldReplace(state: IBlockState): Boolean {
        return isEnabled && !visibleList.contains(state.block.registryName.toString())
    }

    init {
        onToggle {
            mc.renderGlobal.loadRenderers()
        }

        visibleList.editListeners.add {
            mc.renderGlobal.loadRenderers()
        }
    }
}
