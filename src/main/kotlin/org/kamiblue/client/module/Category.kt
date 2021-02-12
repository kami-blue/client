package org.kamiblue.client.module

import org.kamiblue.commons.interfaces.DisplayEnum

enum class Category(override val displayName: String) : DisplayEnum {
    CHAT("Chat"),
    CLIENT("Client"),
    COMBAT("Combat"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render");

    override fun toString() = displayName
}