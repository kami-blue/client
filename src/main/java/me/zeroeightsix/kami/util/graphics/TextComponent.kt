package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.opengl.GL11.*
import kotlin.math.max
import kotlin.math.round

/**
 * Renders multi line text easily
 */
class TextComponent(val separator: String = " ") {
    private val fontRenderer = Wrapper.minecraft.fontRenderer
    private val textLines = ArrayList<TextLine?>()
    var currentLine = 0
        set(value) {
            field = max(value, 0)
        } // Can not be smaller than 0

    /**
     * Create a new copy of a text component
     */
    constructor(textComponent: TextComponent): this(textComponent.separator) {
        this.textLines.addAll(textComponent.textLines)
        this.currentLine = textComponent.currentLine
    }

    /**
     * Create a new text component from a multi line string
     */
    constructor(string: String, separator: String = " ", vararg delimiters: String = arrayOf(separator)): this(separator) {
        val lines = string.lines()
        for (line in lines) {
            for (splitText in line.split(delimiters = *delimiters)) {
                add(splitText)
            }
            if (line != lines.last()) currentLine++
        }
    }

    /**
     * Adds new text element to current line, and goes to the next line
     */
    fun addLine(text: String, color: ColorHolder = ColorHolder(255, 255, 255), style: TextStyle = TextStyle.REGULAR) {
        add(text, color, style)
        currentLine++
    }

    /**
     * Adds new text element to current line, and goes to the next line
     */
    fun addLine(text: String, color: Int, styleIn: TextStyle = TextStyle.REGULAR) {
        add(text, color, styleIn)
        currentLine++
    }

    /**
     * Adds new text element to current line, and goes to the next line
     */
    fun addLine(textElement: TextElement) {
        add(textElement)
        currentLine++
    }

    /**
     * Adds new text element to current line
     */
    fun add(text: String, color: ColorHolder = ColorHolder(255, 255, 255), style: TextStyle = TextStyle.REGULAR) {
        add(text, color.toHex(), style)
    }

    /**
     * Adds new text element to current line
     */
    fun add(text: String, color: Int, style: TextStyle = TextStyle.REGULAR) {
        add(TextElement(text, color, style))
    }

    /**
     * Adds new text element to current line
     */
    fun add(textElement: TextElement) {
        // Adds new lines until we reached the current line
        while (textLines.size <= currentLine) textLines.add(null)

        // Add text element to current line, and create new text line object if current line has null
        textLines[currentLine] = (textLines[currentLine] ?: TextLine(separator)).apply { this.add(textElement) }
    }

    /**
     * Draws all lines in this component
     */
    fun draw(pos: Vec2d = Vec2d(0.0, 0.0),
             lineSpace: Int = 2,
             scale: Float = 1f,
             drawShadow: Boolean = true,
             skipEmptyLine: Boolean = true,
             horizontalAlign: HAlign = HAlign.LEFT,
             verticalAlign: VAlign = VAlign.TOP
    ) {
        if (isEmpty()) return
        glPushMatrix()
        glTranslated(round(pos.x), round(pos.y) + 1.0, 0.0) // Rounding it to int so stupid Minecraftia doesn't fucked up
        glScalef(scale, scale, 1f)
        if (verticalAlign != VAlign.TOP) {
            var height = getHeight(lineSpace)
            if (verticalAlign == VAlign.CENTER) height /= 2
            glTranslatef(0f, -height.toFloat(), 0f)
        }
        for (line in textLines) {
            if (skipEmptyLine && (line == null || line.isEmpty())) continue
            line?.drawLine(fontRenderer, drawShadow, horizontalAlign)
            glTranslatef(0f, (fontRenderer.FONT_HEIGHT + lineSpace).toFloat(), 0f)
        }
        glPopMatrix()
    }

    fun isEmpty() = textLines.firstOrNull { it?.isEmpty() == false } == null

    fun getWidth() = textLines.map { it?.getWidth(fontRenderer) ?: 0 }.max() ?: 0

    fun getHeight(lineSpace: Int, skipEmptyLines: Boolean = false) = fontRenderer.FONT_HEIGHT * getLines(skipEmptyLines) + lineSpace * (getLines(skipEmptyLines) - 1)

    fun getLines(skipEmptyLines: Boolean = true) = textLines.count { !skipEmptyLines || (it != null && !it.isEmpty()) }

    override fun toString() = textLines.joinToString(separator = "\n")

    private class TextLine(val separator: String) {
        private val textElementList = ArrayList<TextElement>()

        fun isEmpty() = textElementList.size == 0

        fun add(textElement: TextElement) {
            textElementList.add(textElement)
        }

        fun drawLine(fontRenderer: FontRenderer, drawShadow: Boolean = false, horizontalAlign: HAlign) {
            glPushMatrix()
            if (horizontalAlign != HAlign.LEFT) {
                var width = getWidth(fontRenderer)
                if (horizontalAlign == HAlign.CENTER) width /= 2
                glTranslatef(-width.toFloat(), 0f, 0f)
            }
            for (textElement in textElementList) {
                val width = fontRenderer.getStringWidth(textElement.text + separator)
                fontRenderer.drawString(textElement.text, 0f, 0f, textElement.color, drawShadow)
                glTranslatef(width.toFloat(), 0f, 0f)
            }
            glPopMatrix()
        }

        fun getWidth(fontRenderer: FontRenderer): Int {
            return fontRenderer.getStringWidth(toString())
        }

        override fun toString() = textElementList.joinToString(separator = separator)
    }

    class TextElement(textIn: String, val color: Int = 0xFFFFFF, val style: TextStyle = TextStyle.REGULAR) {
        val text = "§r${style.code}$textIn"

        override fun toString(): String {
            return text
        }
    }

    @Suppress("UNUSED")
    enum class TextStyle(val code: String) {
        REGULAR(""),
        BOLD("§l"),
        ITALIC("§o"),
        UNDERLINE("§n"),
        STRIKETHROUGH("§m"),
        OBFUSCATED("§k"),
    }

    @Suppress("UNUSED")
    enum class HAlign {
        LEFT, CENTER, RIGHT
    }

    @Suppress("UNUSED")
    enum class VAlign {
        TOP, CENTER, BOTTOM
    }
}