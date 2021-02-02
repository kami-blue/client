package org.kamiblue.client.util.graphics.font

import org.kamiblue.commons.interfaces.DisplayEnum

enum class HAlign(override val displayName: String, val multiplier: Float, val offset: Float) : DisplayEnum {
    LEFT("Left", 0.0f, -1.0f),
    CENTER("Center", 0.5f, 0.0f),
    RIGHT("Right", 1.0f, 1.0f)
}

enum class VAlign(override val displayName: String, val multiplier: Float, val offset: Float) : DisplayEnum {
    TOP("Top", 0.0f, -1.0f),
    CENTER("Center", 0.5f, 0.0f),
    BOTTOM("Bottom", 1.0f, 1.0f)
}