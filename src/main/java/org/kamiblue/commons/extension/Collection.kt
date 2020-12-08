package org.kamiblue.commons.extension

fun <E: Any> MutableCollection<E>.add(e: E?) {
    if (e != null) this.add(e)
}