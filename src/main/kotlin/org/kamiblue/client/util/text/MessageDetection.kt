package org.kamiblue.client.util.text

import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.module.modules.chat.ChatEncryption
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.Wrapper

object MessageDetection {
    enum class Command : PrefixDetector {
        KAMI_BLUE {
            override val prefixes: Array<out CharSequence>
                get() = arrayOf(CommandManager.prefix)
        },
        BARITONE {
            override val prefixes: Array<out CharSequence>
                get() = arrayOf(BaritoneUtils.prefix, "${CommandManager.prefix}b", ".b")
        },
        ANY_EXCEPT_DELIMITER {
            override val prefixes: Array<out CharSequence>
                get() = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "#", "$", CommandManager.prefix)
        },
        ANY {
            override val prefixes: Array<out CharSequence>
                get() = arrayOf(*ANY_EXCEPT_DELIMITER.prefixes, ChatEncryption.delimiter)
        }
    }

    enum class Message : Detector, PlayerDetector, RemovableDetector {
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
            private val regex = "^<(\\w+)>".toRegex()

            override fun detect(input: CharSequence) = playerName(input) != null

            override fun playerName(input: CharSequence) = Wrapper.player?.name?.let { name ->
                regex.find(input)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() && it != name }
            }
        },
        ANY {
            private val regex = "^<(\\w+)>".toRegex()

            override fun detect(input: CharSequence) = input.contains(regex)

            override fun playerName(input: CharSequence) =
                regex.find(input)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
        };

        override fun removedOrNull(input: CharSequence): CharSequence? = playerName(input)?.let {
            input.removePrefix("<$it>")
        }
    }

    enum class Direct(override vararg val regexes: Regex) : RegexDetector, PlayerDetector {
        SENT("^To (\\w+?): ".toRegex(RegexOption.IGNORE_CASE)),
        RECEIVE(
            "^(\\w+?) whispers( to you)?: ".toRegex(),
            "^\\[?(\\w+?)( )?->( )?\\w+?]?( )?:? ".toRegex(),
            "^From (\\w+?): ".toRegex(RegexOption.IGNORE_CASE),
            "^. (\\w+?) » \\w+? » ".toRegex()
        ),
        ANY(*SENT.regexes, *RECEIVE.regexes);

        override fun playerName(input: CharSequence) = matchedRegex(input)?.let { regex ->
            regex.find(input)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
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
        TPA_REQUEST("^\\w+? (has requested|wants) to teleport to you\\.".toRegex())
    }
}
