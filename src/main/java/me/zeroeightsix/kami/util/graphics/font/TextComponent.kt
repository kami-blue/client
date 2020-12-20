package me.zeroeightsix.kami.util.graphics.font

import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.math.Vec2d
import org.lwjgl.opengl.GL11.*
import kotlin.math.max

/**
 * Renders multi line text easily
 */
class TextComponent(val separator: String = "  ") {
    private val textLines = ArrayList<TextLine?>()
    var currentLine = 0
        set(value) {
            field = max(value, 0)
        } // Can not be smaller than 0

    /**
     * Create a new copy of a text component
     */
    constructor(textComponent: TextComponent) : this(textComponent.separator) {
        this.textLines.addAll(textComponent.textLines)
        this.currentLine = textComponent.currentLine
    }

    /**
     * Create a new text component from a multi line string
     */
    constructor(string: String, separator: String = "  ", vararg delimiters: String = arrayOf(separator)) : this(separator) {
        val lines = string.lines()
        for (line in lines) {
            for (splitText in line.split(delimiters = delimiters)) {
                add(splitText)
            }
            if (line != lines.last()) currentLine++
        }
    }

    /**
     * Adds new text element to [currentLine], and goes to the next line
     */
    fun addLine(text: String, color: ColorHolder = ColorHolder(255, 255, 255), style: Style = Style.REGULAR) {
        add(text, color, style)
        currentLine++
    }

    /**
     * Adds new text element to [currentLine], and goes to the next line
     */
    fun addLine(textElement: TextElement) {
        add(textElement)
        currentLine++
    }

    /**
     * Adds new text element to [currentLine]
     */
    fun add(text: String, color: ColorHolder = ColorHolder(255, 255, 255), style: Style = Style.REGULAR) {
        add(TextElement(text, color, style))
    }

    /**
     * Adds new text element to [currentLine]
     */
    fun add(textElement: TextElement) {
        // Adds new lines until we reached the current line
        while (textLines.size <= currentLine) textLines.add(null)

        // Add text element to current line, and create new text line object if current line has null
        textLines[currentLine] = (textLines[currentLine] ?: TextLine(separator)).apply { this.add(textElement) }
    }

    /**
     * Clear all lines in this component, and reset [currentLine]
     */
    fun clear() {
        textLines.clear()
        currentLine = 0
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
             verticalAlign: VAlign = VAlign.TOP,
             customFont: Boolean = FontRenderAdapter.useCustomFont
    ) {
        if (isEmpty()) return
        glPushMatrix()
        glTranslated(pos.x, pos.y, 0.0) // Rounding it to int so stupid Minecraftia doesn't fucked up
        glScalef(scale, scale, 1f)
        if (verticalAlign != VAlign.TOP) {
            var height = getHeight(lineSpace, customFont)
            if (verticalAlign == VAlign.CENTER) height /= 2
            glTranslatef(0f, -height, 0f)
        }
        for (line in textLines) {
            if (skipEmptyLine && (line == null || line.isEmpty())) continue
            line?.drawLine(drawShadow, horizontalAlign, customFont)
            glTranslatef(0f, (FontRenderAdapter.getFontHeight(customFont = customFont) + lineSpace), 0f)
        }
        glPopMatrix()
    }

    fun isEmpty() = textLines.firstOrNull { it?.isEmpty() == false } == null

    fun getWidth(customFont: Boolean = FontRenderAdapter.useCustomFont) = textLines.map {
        it?.getWidth(customFont) ?: 0f
    }.maxOrNull() ?: 0f

    fun getHeight(lineSpace: Int, skipEmptyLines: Boolean = false, customFont: Boolean = FontRenderAdapter.useCustomFont) =
        FontRenderAdapter.getFontHeight(customFont = customFont) * getLines(skipEmptyLines) + lineSpace * (getLines(skipEmptyLines) - 1)

    fun getLines(skipEmptyLines: Boolean = true) = textLines.count { !skipEmptyLines || (it != null && !it.isEmpty()) }

    override fun toString() = textLines.joinToString(separator = "\n")

    private class TextLine(val separator: String) {
        private val textElementList = ArrayList<TextElement>()

        fun isEmpty() = textElementList.size == 0

        fun add(textElement: TextElement) {
            textElementList.add(textElement)
        }

        fun drawLine(drawShadow: Boolean, horizontalAlign: HAlign, customFont: Boolean) {
            glPushMatrix()
            if (horizontalAlign != HAlign.LEFT) {
                var width = getWidth(customFont)
                if (horizontalAlign == HAlign.CENTER) width /= 2
                glTranslatef(-width, 0f, 0f)
            }
            for (textElement in textElementList) {
                FontRenderAdapter.drawString(textElement.text, drawShadow = drawShadow, color = textElement.color, customFont = customFont)
                glTranslatef(FontRenderAdapter.getStringWidth(textElement.text + separator, customFont = customFont), 0f, 0f)
            }
            glPopMatrix()
        }

        fun getWidth(customFont: Boolean = FontRenderAdapter.useCustomFont) = FontRenderAdapter.getStringWidth(toString(), customFont = customFont)

        override fun toString() = textElementList.joinToString(separator = separator)
    }

    class TextElement(textIn: String, val color: ColorHolder = ColorHolder(255, 255, 255), val style: Style = Style.REGULAR) {
        val text = "${style.code}$textIn"

        override fun toString(): String {
            return text
        }
    }
}