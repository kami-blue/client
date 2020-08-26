package me.zeroeightsix.kami.manager

import me.zeroeightsix.kami.KamiMod

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/18/20
 */
open class Manager {

    /**
     * Dummy method for instance creation
     */
    fun new() {
        KamiMod.EVENT_BUS.subscribe(this)
    }
}