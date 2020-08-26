package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.player.AttackEntityEvent

/**
 * @author polymer
 * @author cookiedragon234
 * Updated by polymer 10 March 2020
 * Updated by dominikaaaa on 12/04/20
 * Updated by humboldt123 on July 3rd, 2020
 */
@Module.Info(
        name = "AutoEZ",
        category = Module.Category.COMBAT,
        description = "Sends an insult in chat after killing someone"
)
class AutoEZ : Module() {
    @JvmField
    var mode: Setting<Mode> = register(Settings.e("Mode", Mode.ONTOP))

    @JvmField
    var customText: Setting<String> = register(Settings.stringBuilder("CustomText").withValue("unchanged").withConsumer { _: String?, _: String? -> }.build())

    var hypixelCensorMessages: Array<String> = arrayOf(
            "Hey Helper, how play game?",
            "You’re a great person! Do you want to play some Hypixel games with me?",
            "Your personality shines brighter than the sun!",
            "Welcome to the hypixel zoo!",
            "Maybe we can have a rematch?",
            "In my free time I like to watch cat videos on youtube",
            "I heard you like minecraft, so I built a computer so you can minecraft, while minecrafting in your minecraft.",
            "I like pineapple on my pizza",
            "I had something to say, then I forgot it.",
            "Hello everyone! I’m an innocent player who loves everything Hypixel.",
            "I like Minecraft pvp but you are truly better than me!",
            "Behold, the great and powerful, my magnificent and almighty nemesis!",
            "When nothing is right, go left.",
            "Let’s be friends instead of fighting okay?",
            "Your Clicks per second are godly.",
            "If the world in Minecraft is infinite how can the sun revolve around it?",
            "Pls give me doggo memes!",
            "Blue is greenier than purple for sure",
            "I sometimes try to say bad things and then this happens :(",
            "I have really enjoyed playing with you! <3",
            "What can’t the Ender Dragon read a book? Because he always starts at the End.",
            "You are very good at this game friend.",
            "I like to eat pasta, do you prefer nachos?",
            "Sometimes I sing soppy, love songs in the car.",
            "I love the way your hair glistens in the light",
            "In my free time I like to watch cat videos on youtube",
            "When I saw the guy with a potion I knew there was trouble brewing.",
            "I enjoy long walks on the beach and playing Hypixel",
            "Doin a bamboozle fren.",
            "I need help, teach me how to play!",
            "Can you paint with all the colors of the wind"
    ) // Got these from the forums, kinda based -humboldt123

    private var focus: EntityPlayer? = null
    private var hasBeenCombat = 0

    enum class Mode {


        GG("gg, \$NAME"),
        ONTOP("KAMI BLUE on top! ez \$NAME"),
        EZD("You just got ez'd \$NAME"),
        EZ_HYPIXEL("\$HYPIXEL_MESSAGE \$NAME"),
        NAENAE("You just got naenae'd by kami blue plus, \$NAME"), CUSTOM;

        var text: String? = null

        constructor(text: String?) {
            this.text = text
        }

        constructor() // yes
    }

    private fun getText(m: Mode, playerName: String): String {
        return if (m == Mode.CUSTOM) {
            customText.value.replace("\$NAME", playerName).replace("\$HYPIXEL_MESSAGE", hypixelCensorMessages.random())
        } else {
            m.text!!.replace("\$NAME", playerName).replace("\$HYPIXEL_MESSAGE", hypixelCensorMessages.random())
        }


    }

    @EventHandler
    private val livingDeathEventListener = Listener(EventHook { event: AttackEntityEvent ->
        if (event.target is EntityPlayer) {
            focus = event.target as EntityPlayer
            if (event.entityPlayer.uniqueID === mc.player.uniqueID) {
                if (focus!!.health <= 0.0 || focus!!.isDead || !mc.world.playerEntities.contains(focus)) {
                    mc.player.sendChatMessage(getText(mode.value, event.target.name))
                    return@EventHook
                }
                hasBeenCombat = 1000
            }
        }
    })

    @EventHandler
    private val listener = Listener(EventHook { event: Displayed ->
        if (event.screen !is GuiGameOver) return@EventHook
        if (mc.player.health > 0) {
            hasBeenCombat = 0
        }
    })

    override fun onUpdate() {
        if (mc.player == null) return
        if (hasBeenCombat > 0 && (focus!!.health <= 0.0f || focus!!.isDead || !mc.world.playerEntities.contains(focus))) {
            mc.player.sendChatMessage(getText(mode.value, focus!!.name))
            hasBeenCombat = 0
        }
        --hasBeenCombat

        if (customText.value != "unchanged") return
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 5000 <= System.currentTimeMillis()) { // 5 seconds in milliseconds
            if (mode.value == Mode.CUSTOM && customText.value.equals("unchanged", ignoreCase = true) && mc.player != null) {
                MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the custom " + name + ", please run the &7" + Command.getCommandPrefix() + "autoez&r command to change it, with '&7\$NAME&f' being the username of the killed player")
            }
            startTime = System.currentTimeMillis()
        }
    }

    companion object {
        private var startTime: Long = 0
    }
}
