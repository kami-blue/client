package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.ClientChatReceivedEvent

@Module.Info(
        name = "AutoEZ",
        category = Module.Category.COMBAT,
        description = "Sends an insult in chat after killing someone"
)
object AutoEZ : Module() {
    private val detectMode = register(Settings.e<DetectMode>("DetectMode", DetectMode.HEALTH))
    val messageMode: Setting<MessageMode> = register(Settings.e("MessageMode", MessageMode.ONTOP))
    val customText: Setting<String> = register(Settings.stringBuilder("CustomText").withValue("unchanged"))

    private enum class DetectMode {
        BROADCAST, HEALTH
    }

    @Suppress("UNUSED")
    enum class MessageMode(val text: String) {
        GG("gg, \$NAME"),
        ONTOP("KAMI BLUE on top! ez \$NAME"),
        EZD("You just got ez'd \$NAME"),
        EZ_HYPIXEL("\$HYPIXEL_MESSAGE \$NAME"),
        NAENAE("You just got naenae'd by kami blue plus, \$NAME"),
        CUSTOM("");
    }

    private val hypixelCensorMessages = arrayOf(
            "Hey Helper, how play game?",
            "Hello everyone! I am an innocent player who loves everything Hypixel.",
            "Please go easy on me, this is my first game!",
            "I like long walks on the beach and playing Hypixel",
            "Anyone else really like Rick Astley?",
            "Wait... This isn't what I typed!",
            "Plz give me doggo memes!",
            "You’re a great person! Do you want to play some Hypixel games with me?",
            "Welcome to the hypixel zoo!",
            "If the Minecraft world is infinite, how is the sun spinning around it?",
            "Your clicks per second are godly. ",
            "Maybe we can have a rematch?",
            "Pineapple doesn't go on pizza!",
            "ILY <3",
            "I heard you like Minecraft, so I built a computer in Minecraft in your Minecraft so you can Minecraft while you Minecraft",
            "Why can't the Ender Dragon read a book? Because he always starts at the End.",
            "I sometimes try to say bad things then this happens ",
            "Your personality shines brighter than the sun.",
            "You are very good at the game friend.",
            "I like pasta, do you prefer nachos?",
            "In my free time I like to watch cat videos on youtube",
            "I heard you like minecraft, so I built a computer so you can minecraft, while minecrafting in your minecraft.",
            "I like pineapple on my pizza",
            "You're a great person! Do you want to play some Hypixel games with me?",
            "I had something to say, then I forgot it.",
            "Hello everyone! I’m an innocent player who loves everything Hypixel.",
            "I like Minecraft pvp but you are truly better than me!",
            "Behold, the great and powerful, my magnificent and almighty nemesis!",
            "When nothing is right, go left.",
            "Let’s be friends instead of fighting okay?",
            "Your Clicks per second are godly.",
            "If the world in Minecraft is infinite how can the sun revolve around it?",
            "Blue is greenier than purple for sure",
            "I sometimes try to say bad things and then this happens :(",
            "I have really enjoyed playing with you! <3",
            "What can’t the Ender Dragon read a book? Because he always starts at the End.",
            "You are very good at this game friend.",
            "I like to eat pasta, do you prefer nachos?",
            "Sometimes I sing soppy, love songs in the car.",
            "I love the way your hair glistens in the light",
            "When I saw the guy with a potion I knew there was trouble brewing.",
            "I enjoy long walks on the beach and playing Hypixel",
            "I need help, teach me how to play!",
            "What happens if I add chocolate milk to macaroni and cheese?",
            "Can you paint with all the colors of the wind"
    ) // Got these from the forums, kinda based -humboldt123 

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)
    private val attackedPlayers = LinkedHashMap<EntityPlayer, Int>() // <Player, Last Attack Time>
    private val lockObject = Any()

    init {
        listener<ClientChatReceivedEvent> {
            if (detectMode.value != DetectMode.BROADCAST || mc.player == null
                    || mc.player.isDead || mc.player.health <= 0.0f) return@listener

            val message = it.message.unformattedText
            if (!message.contains(mc.player.name, true)) return@listener
            for (player in attackedPlayers.keys) {
                if (!message.contains(player.name, true)) continue
                sendEzMessage(player)
                break // Break right after removing so we don't get exception
            }
        }

        listener<SafeTickEvent> {
            if (mc.player.isDead || mc.player.health <= 0.0f) {
                attackedPlayers.clear()
                return@listener
            }

            // Update attacked Entity
            val attacked = mc.player.lastAttackedEntity
            if (attacked is EntityPlayer && !attacked.isDead && attacked.health > 0.0f) {
                attackedPlayers[attacked] = mc.player.lastAttackedEntityTime
            }

            // Check death
            if (detectMode.value == DetectMode.HEALTH) {
                for (player in attackedPlayers.keys) {
                    if (!player.isDead && player.health > 0.0f) continue
                    sendEzMessage(player)
                    break // Break right after removing so we don't get exception
                }
            }

            // Remove players if they are out of world or we haven't attack them again in 100 ticks (5 seconds)
            attackedPlayers.entries.removeIf { !it.key.isAddedToWorld || mc.player.ticksExisted - it.value > 100 }

            // Send custom message type help message
            sendHelpMessage()
        }

        // Clear the map on disconnect
        // Disconnect event can be called from another thread so we have to lock it here
        listener<ConnectionEvent.Disconnect> {
            synchronized(lockObject) {
                attackedPlayers.clear()
            }
        }
    }

    private fun sendHelpMessage() {
        if (messageMode.value == MessageMode.CUSTOM && customText.value == "unchanged" && timer.tick(5L)) { // 5 seconds delay
            MessageSendHelper.sendChatMessage("$chatName In order to use the custom " + name
                    + ", please run the &7" + Command.commandPrefix.value
                    + "autoez&r command to change it, with '&7\$NAME&f' being the username of the killed player")
        }
    }

    private fun sendEzMessage(player: EntityPlayer) {
        val text = (if (messageMode.value == MessageMode.CUSTOM) customText.value else messageMode.value.text)
                .replace("\$NAME", player.name).replace("\$HYPIXEL_MESSAGE", hypixelCensorMessages.random())
        MessageSendHelper.sendServerMessage(text)
        attackedPlayers.remove(player)
    }
}
