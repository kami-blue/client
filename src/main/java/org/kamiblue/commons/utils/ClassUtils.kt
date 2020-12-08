package org.kamiblue.commons.utils

import org.reflections.Reflections

object ClassUtils {
    fun <T> findClasses(pack: String, subType: Class<T>): List<Class<out T>> {
        val reflections = Reflections(pack)
        return reflections.getSubTypesOf(subType).sortedBy { it.simpleName }
    }

    inline fun <reified T> getInstance(clazz: Class<out T>): T {
        return clazz.getDeclaredField("INSTANCE")[null] as T
    }
}