package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC
import me.zeroeightsix.kami.mixin.extension.historyBuffer
import me.zeroeightsix.kami.mixin.extension.sentHistoryCursor
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils.useVbo
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawRectOutline
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.gui.GuiChat
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.*

open class KamiGuiChat(startStringIn: String, historyBufferIn: String?, sentHistoryCursorIn: Int) : GuiChat(startStringIn) {

    private var commandHint = ""

    private val regex = " (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()
    private val bracketRegex = "<.*>|\\[.*]".toRegex()

    init {
        if (startStringIn != Command.getCommandPrefix()) calculateCommand(startStringIn.substring(Command.getCommandPrefix().length))
        historyBufferIn?.let { historyBuffer = it }
        sentHistoryCursor = sentHistoryCursorIn
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (Command.getCommandPrefix() != null && !inputField.text.startsWith(Command.getCommandPrefix())) {
            displayNormalChatGUI()
            return
        }

        if (inputField.text == Command.getCommandPrefix()) {
            commandHint = ""
            return
        }

        calculateCommand(inputField.text.substring(Command.getCommandPrefix().length))
    }

    private fun displayNormalChatGUI() {
        GuiChat(inputField.text).apply {
            historyBuffer = this@KamiGuiChat.historyBuffer
            sentHistoryCursor = this@KamiGuiChat.sentHistoryCursor
        }.also {
            mc.displayGuiScreen(it)
        }
    }


    private fun calculateCommand(line: String) {
        val args = line.split(regex).toTypedArray()
        if (args.isEmpty()) return

        val command = getCommand(args[0]) ?: run {
            commandHint = ""
            return
        }

        if (command.syntaxChunks == null || command.syntaxChunks.isEmpty()) return

        val firstAlias = command.aliases.find { it.startsWith(args[0], true) } ?: command.label

        commandHint = firstAlias.substring(args[0].length)

        if (!line.endsWith(" ")) commandHint += " "

        var cutSpace = false

        for ((index, chunk) in command.syntaxChunks.withIndex()) {
            if (index < args.size - 2) continue
            val chunkValue = if (index == args.size - 2) args[index + 1] else null
            val result = chunk.getChunk(command.syntaxChunks, chunk, args, chunkValue)
            val space = if (result.isEmpty()) "" else " "

            cutSpace = cutSpace || result.isNotEmpty() && !result.matches(bracketRegex)
            commandHint += result + space
        }

        if (cutSpace) commandHint = commandHint.substring(1)

        val autoComplete = getAutoComplete()

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && !autoComplete.contains("<") && !autoComplete.contains("[")) {
            inputField.text += getAutoComplete()
            commandHint = ""
        }
    }

    private fun getCommand(string: String): Command? {
        val treeMap = TreeMap<String, Command>()

        for (command in KamiMod.INSTANCE.commandManager.commands) {
            if (command.label.startsWith(string, true)) {
                treeMap[command.label] = command
            } else {
                command.aliases?.let {
                    for (alias in it) {
                        if (!alias.startsWith(string)) continue
                        if (alias == string) continue
                        treeMap[alias] = command
                    }
                }
            }
        }

        return treeMap.firstEntry()?.value
    }

    private fun getAutoComplete(): String {
        return commandHint.split(" <").first().split(" [").first()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawRect(2, height - 14, width - 2, height - 2, Int.MIN_VALUE)

        val x = mc.fontRenderer.getStringWidth(inputField.text + "") + 4
        val y = if (inputField.enableBackgroundDrawing) inputField.y + (inputField.height - 8) / 2 else inputField.y
        mc.fontRenderer.drawStringWithShadow(commandHint, x.toFloat(), y.toFloat(), 0x666666)
        inputField.drawTextBox()


        val chatComponent = mc.ingameGUI.chatGUI.getChatComponent(Mouse.getX(), Mouse.getY())
        if (chatComponent != null && chatComponent.style.hoverEvent != null) {
            handleComponentHover(chatComponent, mouseX, mouseY)
        }

        val vertexHelper = VertexHelper(useVbo())
        val pos1 = Vec2d(inputField.x - 2.0, inputField.y - 2.0)
        val pos2 = pos1.add(inputField.width.toDouble(), inputField.height.toDouble())
        drawRectOutline(vertexHelper, pos1, pos2, 1.5f, ColorHolder(GuiC.windowOutline.color))
    }
}