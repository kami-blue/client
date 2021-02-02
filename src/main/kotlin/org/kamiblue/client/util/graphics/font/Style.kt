package org.kamiblue.client.util.graphics.font

import java.awt.Font

@Suppress("UNUSED")
enum class Style(val code: String, val codeChar: Char, val fontPath: String, val styleConst: Int) {
    REGULAR("§r", 'r', "/assets/kamiblue/fonts/Lato/Lato-Regular.ttf", Font.PLAIN),
    BOLD("§l", 'l', "/assets/kamiblue/fonts/Lato/Lato-Bold.ttf", Font.BOLD),
    ITALIC("§o", 'o', "/assets/kamiblue/fonts/Lato/Lato-Italic.ttf", Font.ITALIC)
}