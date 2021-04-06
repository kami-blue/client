package org.kamiblue.client.module.modules.render

import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockGrass
import net.minecraft.block.BlockStone
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockFluidRenderer
import net.minecraft.client.renderer.BlockRendererDispatcher
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.setting.settings.impl.collection.CollectionSetting

internal object Xray : Module(
    name = "Xray",
    description = "Lets you see through blocks",
    category = Category.RENDER
) {
    private val defaultVisibleList = linkedSetOf("minecraft:diamond_ore", "minecraft:iron_ore", "minecraft:gold_ore")

    val showFluids by setting("Show Fluids", false)

    val visibleList = setting(CollectionSetting("Visible List", defaultVisibleList, { false }))

    fun shouldReplace(state: IBlockState): Boolean {
        return !visibleList.contains(state.block.registryName.toString())
    }

    init {
        onToggle {
            mc.renderGlobal.loadRenderers()
        }

        visibleList.listeners.add {
            mc.renderGlobal.loadRenderers()
        }
    }
}