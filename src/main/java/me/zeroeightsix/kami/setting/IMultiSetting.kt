package me.zeroeightsix.kami.setting

interface IMultiSetting {

    var expanded: Boolean
    val subSettings: Array<out Setting<*>>

}