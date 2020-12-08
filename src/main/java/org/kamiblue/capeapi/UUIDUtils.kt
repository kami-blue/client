package org.kamiblue.capeapi

import java.util.*

object UUIDUtils {
    private val uuidRegex = "[a-z0-9].{7}-[a-z0-9].{3}-[a-z0-9].{3}-[a-z0-9].{3}-[a-z0-9].{11}".toRegex()

    fun fixUUID(string: String): UUID? {
        if (isUUID(string)) return UUID.fromString(string)
        if (string.length < 32) return null
        val fixed = insertDashes(string)
        return if (isUUID(fixed)) UUID.fromString(fixed)
        else null
    }

    fun isUUID(string: String) = uuidRegex.matches(string)

    fun removeDashes(string: String) = string.replace("-", "")

    fun insertDashes(string: String) = StringBuilder(string)
            .insert(8, '-')
            .insert(13, '-')
            .insert(18, '-')
            .insert(23, '-')
            .toString()
}