package me.zeroeightsix.kami.util.translation

import java.util.*

object KamiLang {

    var toReturn = "Test"
    var keys = HashSet<String>()

    fun get(key: TranslationKey) : String{


//        keys.add(key.key)
//        println(key.key)
//        println(keys.size)
        return key.key
    }
}