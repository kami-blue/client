package me.zeroeightsix.kami.manager

import me.zeroeightsix.kami.KamiMod

open class Manager {

    /**
     * Dummy method for instance creation
     */
    fun new() {}

    init {
        KamiMod.log.info("Registering ${this.javaClass.simpleName}...")
    }
}