package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.util.OperatingSystemHelper.OperatingSystem.*

/**
 * @author dominikaaaa
 * @since 14/07/20 19:39
 */
object OperatingSystemHelper {
    /**
     * @return current OperatingSystem
     */
    fun getOS(): OperatingSystem {
        return when {
            System.getProperty("os.name").toLowerCase().contains("nux") -> {
                UNIX
            }
            System.getProperty("os.name").toLowerCase().contains("darwin") || System.getProperty("os.name").toLowerCase().contains("mac") -> {
                OSX
            }
            System.getProperty("os.name").toLowerCase().contains("win") -> {
                WINDOWS
            }
            else -> {
                throw RuntimeException("Operating system couldn't be detected! Report this to the developers")
            }
        }
    }

    /**
     * @return the separator used in filepaths for the current operating system
     */
    fun getFolderSeparator(os: OperatingSystem): Char {
        return when (os) {
            UNIX -> '/'
            OSX -> '/'
            WINDOWS -> '\\'
        }
    }

    enum class OperatingSystem {
        UNIX, OSX, WINDOWS
    }
}