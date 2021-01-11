package me.zeroeightsix.kami.module

import org.kamiblue.commons.collections.NameableSet
import org.kamiblue.commons.interfaces.Alias
import java.util.concurrent.ConcurrentHashMap

class ModuleSet : AbstractMutableSet<Module>() {

    protected val map = ConcurrentHashMap<String, Module>()

    override val size get() = map.size

    fun containsName(name: String): Boolean = map.containsKey(name.toLowerCase())

    fun containsNames(names: Iterable<String>): Boolean = names.all { containsName(it) }

    fun containsNames(names: Array<String>): Boolean = names.all { containsName(it) }

    override fun contains(element: Module): Boolean {
        return map.containsKey(element.name.defaultValue.toLowerCase())
    }

    override fun containsAll(elements: Collection<Module>): Boolean {
        return elements.all { contains(it) }
    }

    override fun iterator() = map.values.iterator()

    operator fun get(name: String) = map[name.toLowerCase()]

    fun getOrPut(name: String, value: () -> Module) = get(name) ?: value().also { add(it) }

    override fun add(element: Module) = map.put(element.name.defaultValue.toLowerCase(), element) == null

    override fun addAll(elements: Collection<Module>): Boolean {
        var modified = false
        elements.forEach {
            modified = add(it) || modified
        }
        return modified
    }

    override fun remove(element: Module) = map.remove(element.name.defaultValue.toLowerCase()) != null

    override fun removeAll(elements: Collection<Module>): Boolean {
        var modified = false
        elements.forEach {
            modified = remove(it) || modified
        }
        return modified
    }

    override fun clear() {
        map.clear()
    }
}