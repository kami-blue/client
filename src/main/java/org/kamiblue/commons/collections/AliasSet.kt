package org.kamiblue.commons.collections

import org.kamiblue.commons.interfaces.Alias

class AliasSet<T : Alias> : NameableSet<T>() {

    override fun add(element: T): Boolean {
        var modified = super.add(element)
        element.alias.forEach {
            modified = map.put(it.toLowerCase(), element) == null || modified
        }
        return modified
    }

    override fun remove(element: T): Boolean {
        var modified = super.remove(element)
        element.alias.forEach {
            modified = map.remove(it.toLowerCase()) != null || modified
        }
        return modified
    }

}