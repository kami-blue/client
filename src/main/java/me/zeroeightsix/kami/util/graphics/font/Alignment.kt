package me.zeroeightsix.kami.util.graphics.font

import org.kamiblue.commons.interfaces.DisplayEnum

enum class HAlign(override val displayName: String, val multiplier: Float) : DisplayEnum {
    LEFT("Left", 0.0f),
    CENTER("Center", 0.5f),
    RIGHT("Right", 1.0f)
}

enum class VAlign(override val displayName: String, val multiplier: Float) : DisplayEnum {
    TOP("Top", 0.0f),
    CENTER("Center", 0.5f),
    BOTTOM("Bottom", 1.0f)
}