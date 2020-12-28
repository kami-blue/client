package me.zeroeightsix.kami.util.text

interface Detector {
    infix fun detect(input: CharSequence): Boolean

    infix fun detectNot(input: CharSequence) = !detect(input)
}

interface LambdaDetector : Detector {
    val filter: (CharSequence) -> Boolean

    override fun detect(input: CharSequence) = filter(input)
}

interface RegexDetector : Detector {
    val regexes: Array<out Regex>

    override infix fun detect(input: CharSequence) = regexes.any { it.matches(input) }

    fun matchedRegex(input: CharSequence) = regexes.find { it.matches(input) }

    fun removedOrNull(input: CharSequence) = matchedRegex(input)?.let { regex ->
        input.replace(regex, "").takeIf { it.isNotBlank() }
    }
}

interface PlayerDetector: Detector {
    fun playerName(input: CharSequence): String?
}