package me.zeroeightsix.kami.setting

import java.io.File

interface IFinalGroup<T> {

    val file: File
    val backup: File

    fun <S : Setting<*>> T.setting(setting: S): S

    fun save()

    fun load()

}