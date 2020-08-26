package me.zeroeightsix.kami.util

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/08/20
 */
class BetterArrayList<E> : ArrayList<E>() {

    /**
     * Appends the specified element to the end of this list if not present.
     *
     * @return true if added.
     */
    fun addIfAbsent(e: E): Boolean {
        return if (!this.contains(e)) {
            this.add(e)
            true
        } else false
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list if not present.
     *
     * @return true if added any elements.
     */
    fun addAllIfAbsent(c: Collection<E>): Boolean {
        var added = false
        for (e in c) {
            added = this.addIfAbsent(e)
        }
        return added
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list if not present.
     *
     * @return true if added any elements.
     */
    fun addAllIfAbsent(c: Array<E>): Boolean {
        var added = false
        for (e in c) {
            added = this.addIfAbsent(e)
        }
        return added
    }
}