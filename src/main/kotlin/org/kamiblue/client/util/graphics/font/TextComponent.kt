package org.kamiblue.client.util.graphics.font

import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.math.Vec2d
import org.lwjgl.opengl.GL11.*
import kotlin.math.max

/**
 * Renders multi line text easily
 */
class TextComponent(val separator: String = " ") {
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
    constructor(string: String, separator: String = " ", vararg delimiters: String = arrayOf(separator)) : this(separator) {
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
    fun addLine(text: String, color: ColorHolder = ColorHolder(255, 255, 255), style: Style = Style.REGULAR, scale: Float = 1f) {
        add(text, color, style, scale)
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
    fun add(text: String, color: ColorHolder = ColorHolder(255, 255, 255), style: Style = Style.REGULAR, scale: Float = 1f) {
        add(TextElement(text, color, style, scale))
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
             alpha: Float = 1.0f,
             scale: Float = 1f,
             drawShadow: Boolean = true,
             skipEmptyLine: Boolean = true,
             horizontalAlign: HAlign = HAlign.LEFT,
             verticalAlign: VAlign = VAlign.TOP,
             customFont: Boolean = FontRenderAdapter.useCustomFont
    ) {
        if (isEmpty()) return

        glPushMatrix()
        glTranslated(pos.x, pos.y - 1.0, 0.0)
        glScalef(scale, scale, 1f)

        if (verticalAlign != VAlign.TOP) {
            var height = getHeight(lineSpace, customFont)
            if (verticalAlign == VAlign.CENTER) height /= 2
            glTranslatef(0f, -height, 0f)
        }

        for (line in textLines) {
            if (skipEmptyLine && (line == null || line.isEmpty())) continue
            line?.drawLine(alpha, drawShadow, horizontalAlign, customFont)
            glTranslatef(0f, (FontRenderAdapter.getFontHeight(customFont = customFont) + lineSpace), 0f)
        }

        glPopMatrix()
    }

    fun isEmpty() = textLines.firstOrNull { it?.isEmpty() == false } == null

    fun getWidth(customFont: Boolean = FontRenderAdapter.useCustomFont) = textLines
        .maxOfOrNull { it?.getWidth(customFont) ?: 0.0f } ?: 0.0f

    fun getHeight(lineSpace: Int, skipEmptyLines: Boolean = true, customFont: Boolean = FontRenderAdapter.useCustomFont) =
        FontRenderAdapter.getFontHeight(customFont = customFont) * getLines(skipEmptyLines) + lineSpace * (getLines(skipEmptyLines) - 1)

    fun getLines(skipEmptyLines: Boolean = true) = textLines.count { !skipEmptyLines || (it != null && !it.isEmpty()) }

    override fun toString() = textLines.joinToString(separator = "\n")

    class TextLine(private val separator: String) {
        private val textElementList = ArrayList<TextElement>()

        fun isEmpty() = textElementList.size == 0

        fun add(textElement: TextElement) {
            textElementList.add(textElement)
        }

        fun drawLine(alpha: Float, drawShadow: Boolean, horizontalAlign: HAlign, customFont: Boolean) {
            glPushMatrix()

            if (horizontalAlign != HAlign.LEFT) {
                var width = getWidth(customFont)
                if (horizontalAlign == HAlign.CENTER) width /= 2
                glTranslatef(-width, 0f, 0f)
            }

            for (textElement in textElementList) {
                val color = textElement.color.clone()
                color.a = (color.a * alpha).toInt()
                FontRenderAdapter.drawString(textElement.text, drawShadow = drawShadow, color = color, customFont = customFont, scale = textElement.scale)
                val adjustedSeparator = if (separator == " " && customFont) "  " else " "
                glTranslatef(FontRenderAdapter.getStringWidth(textElement.text + adjustedSeparator, customFont = customFont), 0f, 0f)
            }

            glPopMatrix()
        }

        fun getWidth(customFont: Boolean = FontRenderAdapter.useCustomFont): Float {
            val adjustedSeparator = if (separator == " " && customFont) "  " else " "
            val string = textElementList.joinToString(separator = adjustedSeparator)
            return FontRenderAdapter.getStringWidth(string, customFont = customFont)
        }

        fun reverse() {
            textElementList.reverse()
        }

    }

    class TextElement(textIn: String, val color: ColorHolder = ColorHolder(255, 255, 255), val style: Style = Style.REGULAR, val scale: Float = 1f) {
        val text = "${style.code}$textIn"

        override fun toString(): String {
            return text
        }
    }
}