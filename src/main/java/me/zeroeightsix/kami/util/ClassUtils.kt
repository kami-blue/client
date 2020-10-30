package me.zeroeightsix.kami.util

import org.reflections.Reflections

/**
 * Created by 086 on 23/08/2017.
 */
object ClassUtils {
    @JvmStatic
    fun <T> findClasses(pack: String?, subType: Class<T>?): List<Class<out T>> {
        val reflections = Reflections(pack)
        return reflections.getSubTypesOf(subType).sortedBy { it.simpleName }
    }

    @JvmStatic
    inline fun <reified T> getInstance(clazz: Class<out T>): T {
       return clazz.getDeclaredField("INSTANCE")[null] as T
    }
}