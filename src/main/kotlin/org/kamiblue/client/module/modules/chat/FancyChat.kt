package org.kamiblue.client.module.modules.chat

import org.kamiblue.client.manager.managers.MessageManager.newMessageModifier
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageDetection
import org.kamiblue.commons.utils.MathUtils
import kotlin.math.min

internal object FancyChat : Module(
    name = "FancyChat",
    category = Category.CHAT,
    description = "Makes messages you send fancy",
    showOnArray = false,
    modulePriority = 100
) {
    private val uwu = setting("uwu", true)
    private val leet = setting("1337", false)
    private val mock = setting("mOcK", false)
    private val green = setting(">", false)
    private val blue = setting("`", false)
    private val randomSetting = setting("Random Case", true, { mock.value })
    private val commands = setting("Commands", false)
    private val spammer = setting("Spammer", false)

    private val modifier = newMessageModifier(
        filter = {
            (commands.value || MessageDetection.Command.ANY detectNot it.packet.message)
                && (spammer.value || it.source !is Spammer)
        },
        modifier = {
            val message = getText(it.packet.message)
            message.substring(0, min(256, message.length))
        }
    )

    init {
        onEnable {
            modifier.enable()
        }

        onDisable {
            modifier.disable()
        }
    }

    private fun getText(s: String): String {
        var string = s
        if (uwu.value) string = uwuConverter(string)
        if (leet.value) string = leetConverter(string)
        if (mock.value) string = mockingConverter(string)
        if (green.value) string = greenConverter(string)
        if (blue.value) string = blueConverter(string)
        return string
    }

    private fun greenConverter(input: String): String {
        return "> $input"
    }

    private fun blueConverter(input: String): String {
        return "`$input"
    }

    override fun getHudInfo(): String {
        val returned = StringBuilder()
        if (uwu.value) {
            returned.append("uwu")
        }
        if (leet.value) {
            returned.append(" 1337")
        }
        if (mock.value) {
            returned.append(" mOcK")
        }
        if (green.value) {
            returned.append(" >")
        }
        if (blue.value) {
            returned.append(" `")
        }
        return returned.toString()
    }

    private fun leetConverter(input: String): String {
        val message = StringBuilder()
        for (element in input) {
            var inputChar = element.toString() + ""
            inputChar = inputChar.toLowerCase()
            inputChar = leetSwitch(inputChar)
            message.append(inputChar)
        }
        return message.toString()
    }

    private fun mockingConverter(input: String): String {
        val message = StringBuilder()
        for (i in input.indices) {
            var inputChar = input[i].toString() + ""
            val rand = if (randomSetting.value) (0..1).random() else 0
            inputChar = if (!MathUtils.isNumberEven(i + rand)) inputChar.toUpperCase() else inputChar.toLowerCase()
            message.append(inputChar)
        }
        return message.toString()
    }

    private fun uwuConverter(input: String): String {
        var lInput = input
        lInput = lInput.replace('L', 'W')
        lInput = lInput.replace('R', 'W')
        lInput = lInput.replace('l', 'w')
        lInput = lInput.replace('r', 'w')
        lInput = lInput.replace(" n", " ny")
        lInput = lInput.replace(" N", " Ny")
        lInput = lInput.replace("ove", "uv")
        lInput = lInput.replace("OVE", "UV")
        lInput = lInput.replace("this", "dis")
        lInput = lInput.replace("no", "nyo")
        lInput = lInput.replace("mo", "myo")
        lInput = lInput.replace("No", "Nyo")
        lInput = lInput.replace("Mo", "Myo")
        lInput = lInput.replace("na", "nya")
        lInput = lInput.replace("ni", "nyi")
        lInput = lInput.replace("nu", "nyu")
        lInput = lInput.replace("ne", "nye")
        lInput = lInput.replace("anye", "ane")
        lInput = lInput.replace("inye", "ine")
        lInput = lInput.replace("onye", "one")
        lInput = lInput.replace("unye", "une")
        return lInput
    }

    private fun leetSwitch(i: String): String {
        return when (i) {
            "a" -> "4"
            "e" -> "3"
            "g" -> "6"
            "l", "i" -> "1"
            "o" -> "0"
            "s" -> "$"
            "t" -> "7"
            else -> i
        }
    }
}
