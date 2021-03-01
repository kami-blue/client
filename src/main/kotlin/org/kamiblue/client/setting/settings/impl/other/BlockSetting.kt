package org.kamiblue.client.setting.settings.impl.other

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.block.Block
import org.kamiblue.client.gui.rgui.windows.BlockPicker
import org.kamiblue.client.setting.settings.MutableSetting

class BlockSetting(
    name: String,
    value: Block,
    visibility: () -> Boolean = { true },
    consumer: (prev: Block, input: Block) -> Block = { _, input -> input },
    description: String = ""
) : MutableSetting<Block>(name, value, visibility, consumer, description) {

    override fun write() = JsonPrimitive(value.translationKey)

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asString.let {
            for (block in BlockPicker.blocks) {
                if (block.translationKey == it) {
                    value = block
                    break
                }
            }
        }
    }
}
