package me.zeroeightsix.kami.util

import org.reflections.Reflections

/**
 * Created by 086 on 23/08/2017.
 */
object ClassFinder {
    @JvmStatic
    fun findClasses(pack: String?, subType: Class<*>?): Array<Class<*>> {
        val reflections = Reflections(pack)
        return reflections.getSubTypesOf(subType).toTypedArray()
    }
}