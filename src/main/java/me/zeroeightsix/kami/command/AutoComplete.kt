package me.zeroeightsix.kami.command

interface AutoComplete {
    fun completeForInput(string: String): String?
}

open class DynamicPrefixMatch(
    private val matchList: () -> Collection<String>?
) : AutoComplete {

    final override fun completeForInput(string: String): String? {
        if (string.isBlank()) return null
        val list = matchList() ?: return null

        val matched = list.stream()
            .filter { it.startsWith(string, true) }
            .findFirst()

        return matched.orElse(null)
    }

}

class StaticPrefixMatch(
    private val matchList: Collection<String>
) : DynamicPrefixMatch({ matchList })