package org.kamiblue.client.gui.rgui.component

import net.minecraft.block.Block
import org.kamiblue.client.gui.rgui.windows.BlockPicker
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.items.item
import org.kamiblue.client.util.math.Vec2f

class BlockButton(val block: Block, isCurrent: Boolean) : Slider(block.localizedName, if (isCurrent) 1.0 else 0.0, "", { true }, 40f, 40f) {

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)

        BlockPicker.notifyAboutClick(block)
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)

        RenderUtils2D.drawItem(block.item.defaultInstance, (width - 20).toInt(), height.toInt())
    }
}