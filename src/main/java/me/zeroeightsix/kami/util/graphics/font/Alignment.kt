package me.zeroeightsix.kami.util.graphics.font

@Suppress("UNUSED")
enum class HAlign(val multiplier: Float) {
    LEFT(0f),
    CENTER(0.5f),
    RIGHT(1f)
}

@Suppress("UNUSED")
enum class VAlign(val multiplier: Float) {
    TOP(0f),
    CENTER(0.5f),
    BOTTOM(1f)
}