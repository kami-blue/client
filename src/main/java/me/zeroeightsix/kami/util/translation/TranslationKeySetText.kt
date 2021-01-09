package me.zeroeightsix.kami.util.translation

class TranslationKeySetText(val text: String) : TranslationKey("SetText") {

    override val value : String
        get() = text
}