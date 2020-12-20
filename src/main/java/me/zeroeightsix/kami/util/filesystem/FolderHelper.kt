package me.zeroeightsix.kami.util.filesystem

import me.zeroeightsix.kami.util.filesystem.OperatingSystemHelper.OperatingSystem.*
import java.io.File

/**
 * @author l1ving
 * @since 14/07/20 19:50
 */
object FolderHelper {
    fun getVersionsFolder(os: OperatingSystemHelper.OperatingSystem): String {
        return getMinecraftFolder(os) + "versions" + OperatingSystemHelper.getFolderSeparator(os)
    }

    fun getModsFolder(os: OperatingSystemHelper.OperatingSystem): String {
        return getMinecraftFolder(os) + "mods" + OperatingSystemHelper.getFolderSeparator(os)
    }

    /**
     * @return the Minecraft folder specific to the current operating system
     */
    fun getMinecraftFolder(os: OperatingSystemHelper.OperatingSystem): String {
        return when (os) {
            UNIX -> System.getProperty("user.home") + "/.minecraft/"
            OSX -> System.getProperty("user.home") + "/Library/Application Support/minecraft/"
            WINDOWS -> System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator
        }
    }
}