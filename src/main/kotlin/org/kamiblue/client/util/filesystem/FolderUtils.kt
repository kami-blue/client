package org.kamiblue.client.util.filesystem

import java.io.File

object FolderUtils {
    @JvmStatic
    val versionsFolder
        get() = "${minecraftFolder}versions${File.separator}"

    @JvmStatic
    val modsFolder
        get() = "${minecraftFolder}mods${File.separator}"

    /**
     * The Minecraft folder specific to the current operating system
     */
    val minecraftFolder: String
        get() = when (getOS()) {
            OperatingSystem.UNIX -> System.getProperty("user.home") + "/.minecraft/"
            OperatingSystem.OSX -> System.getProperty("user.home") + "/Library/Application Support/minecraft/"
            OperatingSystem.WINDOWS -> System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator
        }

    /**
     * @return current OperatingSystem
     */
    private fun getOS(): OperatingSystem {
        val osName = System.getProperty("os.name").toLowerCase()
        return when {
            osName.contains("nux") -> {
                OperatingSystem.UNIX
            }
            osName.contains("darwin") || osName.contains("mac") -> {
                OperatingSystem.OSX
            }
            osName.contains("win") -> {
                OperatingSystem.WINDOWS
            }
            else -> {
                throw RuntimeException("Operating system couldn't be detected! Report this to the developers")
            }
        }
    }

    private enum class OperatingSystem {
        UNIX, OSX, WINDOWS
    }
}