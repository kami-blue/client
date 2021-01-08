package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import java.io.File
import java.util.*

object KamiLang {


    var map: MutableMap<String, String> = HashMap()
    var isLoaded : Boolean = false

    fun load(language: String) {
        isLoaded = true
        println("Loading Kami languages for $language")
        //Load english first, so it will use english if they aren't in the new language file.
        var input = KamiLang::class.java.getResource("/assets/kamiblue/language/en_us.KamiLang")
        var file = File(input.file)
        var scanner = Scanner(file)
        while (scanner.hasNextLine()) {
            val current : Array<String> = scanner.nextLine().split("=").toTypedArray()
            map[current[0]] = current[1]

            print(current[0] + current[1])
        }
        //Now load primary language.
        if (language != "en_us") {
            input = KamiLang::class.java.getResource("/assets/kamiblue/lang/$language.KamiLang")
            file = File(input.file)
            scanner = Scanner(file)
            while (scanner.hasNextLine()) {
                val current: Array<String> = scanner.nextLine().split("=").toTypedArray()
                map[current[0]] = current[1]
                print(current[0] + current[1])
            }
        }
    }

    operator fun get(key: String, vararg objects: Any?): String {

        if (!isLoaded){
            load("en_us");
        }

        println(map.values.toString())
        print(String.format(map[key]!!, *objects))
        return String.format(map[key]!!, *objects)
    }
}