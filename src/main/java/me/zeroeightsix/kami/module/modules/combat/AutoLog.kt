package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.gui.mc.KamiGuiDisconnected
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.AutoLog.Reasons.*
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.text.TextComponentString

@Module.Info(
        name = "AutoLog",
        description = "Automatically log when in danger or on low health",
        category = Module.Category.COMBAT,
        alwaysListening = true
)
object AutoLog : Module() {
    private val disable: Setting<DisableMode> = register(Settings.e("Disable", DisableMode.ALWAYS))
    private val health = register(Settings.integerBuilder("Health").withRange(0, 36).withValue(10).build())
    private val crystals = register(Settings.b("Crystals", false))
    private val creeper = register(Settings.b("Creepers", true))
    private val creeperDistance = register(Settings.integerBuilder("CreeperDistance").withRange(1, 10).withValue(5).withVisibility { creeper.value }.build())
    private val totem = register(Settings.b("Totems", false))
    private val totemAmount = register(Settings.integerBuilder("MinTotems").withRange(1, 10).withValue(2).withVisibility { totem.value }.build())
    private val players = register(Settings.b("Players", false))
    private val playerDistance = register(Settings.integerBuilder("PlayerDistance").withRange(64, 256).withValue(128).withVisibility { players.value }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value }.build())

    override fun onEnable() {
        if (mc.player == null) disable()
    }

    override fun onUpdate() {
        if (isDisabled) return

        if (mc.player.health < health.value) {
            log(HEALTH)
            return
        }

        if (totem.value && totemAmount.value > InventoryUtils.countItemAll(449)) {
            log(TOTEM)
            return
        }

        if (crystals.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity !is EntityEnderCrystal) continue
                if (mc.player.getDistance(entity) > 8f) continue
                if (mc.player.health - CrystalAura.calculateDamage(entity, mc.player) > health.value) continue
                log(END_CRYSTAL)
                return
            }
        }

        if (creeper.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity !is EntityCreeper) continue
                if (mc.player.getDistance(entity) > creeperDistance.value) continue
                log(CREEPER, MathUtils.round(entity.getDistance(mc.player), 2).toString())
                return
            }
        }

        if (players.value) {
            for (entity in mc.world.loadedEntityList) {
                if (entity !is EntityPlayer) continue
                if (entity == mc.player) continue
                if (mc.player.getDistance(entity) > playerDistance.value) continue
                if (!friends.value && Friends.isFriend(entity.name)) continue
                log(PLAYER, entity.name)
                return
            }
        }
    }

    private fun log(reason: Reasons, additionalInfo: String = "") {
        val reasonText = getReason(reason, additionalInfo)
        val screen = getScreen() // do this before disconnecting

        mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        mc.connection?.networkManager?.closeChannel(TextComponentString(""))
        mc.loadWorld(null as WorldClient?)

        mc.displayGuiScreen(KamiGuiDisconnected(reasonText, screen, disable.value == DisableMode.ALWAYS || (disable.value == DisableMode.NOT_PLAYER && reason != PLAYER)))
    }

    private fun getScreen() = if (mc.isIntegratedServerRunning) {
        GuiMainMenu()
    } else {
        GuiMultiplayer(GuiMainMenu())
    }

    private fun getReason(reason: Reasons, additionalInfo: String) = when (reason) {
        HEALTH -> arrayOf("Health went below ${health.value}!")
        TOTEM -> arrayOf("Less then ${totemMessage(totemAmount.value)}!")
        CREEPER -> arrayOf("Creeper came near you!", "It was $additionalInfo blocks away")
        PLAYER -> arrayOf("Player $additionalInfo came within ${playerDistance.value} blocks range!")
        END_CRYSTAL -> arrayOf("An end crystal was placed too close to you!", "It would have done more then ${health.value} damage!")
    }

    private enum class Reasons {
        HEALTH, TOTEM, CREEPER, PLAYER, END_CRYSTAL
    }

    @Suppress("UNUSED")
    private enum class DisableMode {
        NEVER, ALWAYS, NOT_PLAYER
    }

    private fun totemMessage(amount: Int) = if (amount == 1) {
        "one totem"
    } else {
        "$amount totems"
    }
}