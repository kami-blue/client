package me.zeroeightsix.kami.setting.settings

interface IMultiSetting {

    var expanded: Boolean
    val subSettings: Array<out Setting<*>>

}