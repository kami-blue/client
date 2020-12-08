package me.zeroeightsix.kami.util.graphics.font

import java.awt.Font

@Suppress("UNUSED")
enum class Style(val code: String, val codeChar: Char, val fontPath: String, val styleConst: Int) {
    REGULAR("§r", 'r', "/assets/necron/fonts/Bord/Bord.ttf", Font.PLAIN),
    BOLD("§l", 'l', "/assets/necron/fonts/Source_Sans_Pro/SourceSansPro-Black.ttf", Font.BOLD),
    ITALIC("§o", 'o', "/assets/necron/fonts/Source_Sans_Pro/SourceSansPro-SemiBoldItalic.ttf", Font.ITALIC)
}