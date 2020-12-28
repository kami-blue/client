package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.module.modules.chat.ChatEncryption
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.Wrapper

object MessageDetection {
    enum class Command(override val filter: (CharSequence) -> Boolean) : LambdaDetector {
        KAMI_BLUE({ it.startsWith(CommandManager.prefix) }),
        BARITONE({ it.startsWith(BaritoneUtils.prefix) || it.startsWith("${CommandManager.prefix}b") || it.startsWith(".b") }),
        ANY({ input -> commandPrefixes.any { input.startsWith(it) } });

        private companion object {
            private val commandPrefixes: Array<String>
                get() = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "%", "#", "$",
                    CommandManager.prefix,
                    ChatEncryption.delimiterValue.value)
        }
    }

    enum class Message : Detector, PlayerDetector {
        SELF {
            override fun detect(input: CharSequence) = Wrapper.player?.name?.let {
                input.startsWith("<${it}>")
            } ?: false

            override fun playerName(input: CharSequence): String? {
                return if (detectNot(input)) null
                else Wrapper.player?.name
            }
        },
        OTHER {
            private val regex = "^<(\\w)>".toRegex()

            override fun detect(input: CharSequence) = playerName(input) != null

            override fun playerName(input: CharSequence) = Wrapper.player?.name?.let { name ->
                input.replace(regex, "$1").takeIf { it.isNotBlank() && it != name }
            }
        },
        ANY {
            private val regex = "^<(\\w)>".toRegex()

            override fun detect(input: CharSequence) = input.contains(regex)

            override fun playerName(input: CharSequence) = input.replace(regex, "$1").takeIf { it.isNotBlank() }
        }
    }

    enum class Direct(override vararg val regexes: Regex) : RegexDetector, PlayerDetector {
        SENT("^To (\\w+?): ".toRegex(RegexOption.IGNORE_CASE)),
        RECEIVE(
            "^(\\w+?) whispers( to you)?: ".toRegex(),
            "^\\[?(\\w+?)( )?->( )?\\w+?]?( )?:? ".toRegex(),
            "^From (\\w+?): ".toRegex(RegexOption.IGNORE_CASE),
            ". (\\w+?) » Ja » ".toRegex()
        ),
        ANY(*SENT.regexes, *RECEIVE.regexes);

        override fun playerName(input: CharSequence) = matchedRegex(input)?.let { regex ->
            input.replace(regex, "$1").takeIf { it.isNotBlank() }
        }
    }

    enum class Server(override vararg val regexes: Regex) : RegexDetector {
        QUEUE("^Position in queue: ".toRegex()),
        QUEUE_IMPORTANT("^Position in queue: [1-5]$".toRegex()),
        RESTART("^\\[SERVER] Server restarting in ".toRegex()),
        ANY(*QUEUE.regexes, *RESTART.regexes)
    }

    enum class Other(override vararg val regexes: Regex) : RegexDetector {
        BARITONE("^\\[B(aritone)?]".toRegex()),
        TPA_REQUEST("^\\w+? (has requested|wants) to teleport to you\\.".toRegex());
    }
}