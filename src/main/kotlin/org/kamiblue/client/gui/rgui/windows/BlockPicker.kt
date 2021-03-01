package org.kamiblue.client.gui.rgui.windows

import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation
import org.kamiblue.client.gui.rgui.component.BlockButton
import org.kamiblue.client.setting.settings.impl.other.BlockSetting

object BlockPicker : ListWindow("Block Picker", 0.0f, 0.0f, 200.0f, 200.0f, SettingGroup.NONE) {

    var setting: BlockSetting? = null

    override var lastActiveTime: Long = Long.MAX_VALUE // Dodgy thing to create always on top
        set(value) {
            field = Long.MAX_VALUE
        }

    val blocks = Block.REGISTRY.keys.asSequence().map { Block.REGISTRY.getObject(ResourceLocation(it.path)) }.distinctBy { it.localizedName }.filter { !it.localizedName.startsWith("tile") }.sortedBy { it.localizedName.toLowerCase() }.toList()

    override fun onDisplayed() {
        super.onDisplayed()

        for (block in blocks) {
            this.children.add(BlockButton(block, block == setting?.value))
        }
    }

    fun notifyAboutClick(block: Block) {
        setting?.value = block
        setting = null
        visible = false
    }
}