package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod.MODULE_MANAGER
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.LagNotifier
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketPlayerPosLook
import java.lang.Math.random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Created by 086 on 11/04/2018.
 * Updated by Itistheend on 28/12/19.
 * Updated by dominikaaaa on 26/05/20
 * Updated by pNoName on 28/05/20
 * Updated by Xiaro on 02/07/20
 */
@Module.Info(
        name = "ElytraFlight",
        description = "Allows infinite and way easier Elytra flying",
        category = Module.Category.MOVEMENT
)
class ElytraFlight : Module() {
    private val mode = register(Settings.e<ElytraFlightMode>("Mode", ElytraFlightMode.CONTROL))
    private val defaultSetting = register(Settings.b("Defaults", false))
    private val durabilityWarning = register(Settings.b("DurabilityWarning", true))
    private val threshold = register(Settings.integerBuilder("Broken%").withRange(1, 50).withValue(5).withVisibility { durabilityWarning.value }.build())

    /* Takeoff */
    private val easyTakeOff = register(Settings.b("EasyTakeoff", true))
    private val timerControl = register(Settings.booleanBuilder("TakeoffTimer").withValue(true).withVisibility { easyTakeOff.value }.build())

    /* Spoof Pitch */
    private val spoofPitch = register(Settings.booleanBuilder("SpoofPitch").withValue(true).withVisibility { mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE }.build())
    private val forwardPitch = register(Settings.integerBuilder("ForwardPitch").withRange(-90, 90).withValue(0).withVisibility { spoofPitch.value && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE) }.build())

    /* Boost */
    private val speedBoost = register(Settings.floatBuilder("SpeedB").withValue(1.0f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())
    private val upSpeedBoost = register(Settings.floatBuilder("UpSpeedB").withValue(1.0f).withMinimum(0.0f).withMaximum(5.0f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())
    private val downSpeedBoost = register(Settings.floatBuilder("DownSpeedB").withValue(1.0f).withMinimum(0.0f).withMaximum(5.0f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())

    /* Control */
    private val boostPitchControl = register(Settings.integerBuilder("BaseBoostPitch").withRange(0, 90).withValue(20).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val altitudeHoldControl = register(Settings.booleanBuilder("AltitudeHold").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val dynamicDownSpeed = register(Settings.booleanBuilder("DynamicDownSpeed").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val speedControl = register(Settings.floatBuilder("SpeedC").withValue(1.81f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val fallSpeedControl = register(Settings.floatBuilder("FallSpeedC").withValue(0.00000000000003f).withMinimum(0.0f).withMaximum(0.3f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val downSpeedControl = register(Settings.floatBuilder("DownSpeedC").withMaximum(5.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL && !dynamicDownSpeed.value }.build())
    private val minDownSpeedControl = register(Settings.floatBuilder("MinDownSpeedC").withMaximum(5.0f).withMinimum(0.0f).withValue(0.5f).withVisibility { mode.value == ElytraFlightMode.CONTROL && dynamicDownSpeed.value }.build())
    private val maxDownSpeedControl = register(Settings.floatBuilder("MaxDownSpeedC").withMaximum(5.0f).withMinimum(0.0f).withValue(2.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL && dynamicDownSpeed.value }.build())

    /* Creative */
    private val speedCreative = register(Settings.floatBuilder("SpeedCR").withValue(1.8f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.CREATIVE }.build())
    private val fallSpeedCreative = register(Settings.floatBuilder("FallSpeedCR").withValue(0.0001f).withMinimum(0.0f).withMaximum(0.3f).withVisibility { mode.value == ElytraFlightMode.CREATIVE }.build())
    private val upSpeedCreative = register(Settings.floatBuilder("UpSpeedCR").withMaximum(5.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CREATIVE }.build())
    private val downSpeedCreative = register(Settings.floatBuilder("DownSpeedCR").withMaximum(5.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CREATIVE }.build())

    /* Packet */
    private val speedPacket = register(Settings.floatBuilder("SpeedP").withValue(1.8f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.PACKET }.build())
    private val fallSpeedPacket = register(Settings.floatBuilder("FallSpeedP").withValue(0.00001f).withMinimum(0.0f).withMaximum(0.3f).withVisibility { mode.value == ElytraFlightMode.PACKET }.build())
    private val downSpeedPacket = register(Settings.floatBuilder("DownSpeedP").withValue(1.0f).withMinimum(0.0f).withMaximum(5.0f).withVisibility { mode.value == ElytraFlightMode.PACKET }.build())

    private enum class ElytraFlightMode {
        BOOST, CONTROL, CREATIVE, PACKET
    }

    /* Generic states */
    private var elytraIsEquipped = false
    private var elytraDurability = 0
    private var outOfDurability = false
    private var isFlying = false
    private var isPacketFlying = false
    private var isStandingStillH = false
    private var isStandingStill = false

    /* Control mode states */
    private var hoverTarget = -1.0
    private var packetYaw = 0.0f
    private var packetPitch = 0.0f
    private var hoverState = false

    /* Event Handlers */
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (!elytraIsEquipped || elytraDurability <= 1 || mc.player == null || !isFlying || mode.value == ElytraFlightMode.BOOST || mc.player.isSpectator) return@EventHook
        if (event.packet is CPacketPlayer.Rotation || event.packet is CPacketPlayer.PositionRotation) {
            val packet = event.packet as CPacketPlayer
            if (mode.value != ElytraFlightMode.PACKET) {
                /* Cancels rotation packets when standing still and not clicking */
                if (spoofPitch.value && isStandingStill && !mc.gameSettings.keyBindUseItem.isKeyDown && !mc.gameSettings.keyBindAttack.isKeyDown) {
                    event.cancel()
                }

                /* Spoof Pitch */
                if (spoofPitch.value && !isStandingStill) {
                    packet.pitch = packetPitch
                }
            }
            if (mode.value != ElytraFlightMode.CREATIVE) packet.yaw = packetYaw
        }
    })

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (!elytraIsEquipped || elytraDurability <= 1 || mc.player == null || !isFlying || mode.value == ElytraFlightMode.BOOST || mc.player.isSpectator) return@EventHook
        if (event.packet is SPacketPlayerPosLook && mode.value != ElytraFlightMode.PACKET) {
            val packet = event.packet as SPacketPlayerPosLook
            packet.pitch = mc.player.rotationPitch
        }

        /* Cancels the elytra opening animation */
        if (event.packet is SPacketEntityMetadata && isPacketFlying && mode.value == ElytraFlightMode.PACKET) {
            val packet = event.packet as SPacketEntityMetadata
            if (packet.entityId == mc.player.getEntityId()) event.cancel()
        }
    })

    @EventHandler
    private val playerTravelListener = Listener(EventHook { event: PlayerTravelEvent ->
        if (mc.player == null || mc.player.isSpectator) return@EventHook
        stateUpdate(event)

        if (elytraIsEquipped && elytraDurability > 1) {
            if (!isFlying && !isPacketFlying) {
                if (mode.value != ElytraFlightMode.CREATIVE) mc.player.capabilities.isFlying = false /* For fixing issues when switching mode in air */
                takeoff()
            } else {
                mc.timer.tickLength = 50.0f
                mc.player.isSprinting = false

                when (mode.value) {
                    ElytraFlightMode.BOOST -> boostMode()
                    ElytraFlightMode.CONTROL -> controlMode(event)
                    ElytraFlightMode.CREATIVE -> creativeMode()
                    ElytraFlightMode.PACKET -> packetMode(event)
                    else -> return@EventHook
                }
            }
        }
    })
    /* End of Event Handlers */

    /* Generic Functions */
    private fun stateUpdate(event: PlayerTravelEvent) {
        /* Elytra Check */
        val armorSlot = mc.player.inventory.armorInventory[2]
        elytraIsEquipped = armorSlot.getItem() == Items.ELYTRA

        /* Elytra Durability Check */
        if (elytraIsEquipped) {
            val oldDurability = elytraDurability
            elytraDurability = armorSlot.maxDamage - armorSlot.getItemDamage()

            /* Elytra Durability Warning, runs when player is in the air and durability changed */
            if (!mc.player.onGround && oldDurability != elytraDurability) {
                if (durabilityWarning.value && elytraDurability > 1 && elytraDurability < threshold.value * armorSlot.maxDamage / 100) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    sendChatMessage("$chatName Warning: Elytra has " + (elytraDurability - 1) + " durability remaining")
                } else if (elytraDurability <= 1 && !outOfDurability) {
                    outOfDurability = true
                    if (durabilityWarning.value) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                        sendChatMessage("$chatName Elytra is out of durability, holding player in the air")
                    }
                }
            }
        } else elytraDurability = 0

        /* Holds player in the air if run out of durability */
        if (!mc.player.onGround && outOfDurability && elytraDurability <= 1) {
            event.cancel()
            mc.player.setVelocity(0.0, -0.01, 0.0)
        } else if (outOfDurability) outOfDurability = false /* Reset if players is on ground or replace with a new elytra */

        /* Elytra flying status check */
        isFlying = mc.player.isElytraFlying || (mc.player.capabilities.isFlying && mode.value == ElytraFlightMode.CREATIVE)
        isPacketFlying = isPacketFlying && mode.value == ElytraFlightMode.PACKET

        /* Movement input check */
        if (isFlying) {
            isStandingStillH = mc.player.movementInput.moveForward == 0f && mc.player.movementInput.moveStrafe == 0f
            isStandingStill = isStandingStillH && !mc.player.movementInput.jump && !mc.player.movementInput.sneak
        } else isStandingStill = false
    }

    /* The best takeoff method <3 */
    private fun takeoff() {
        /* Pause Takeoff if server is lagging */
        val lagNotifier = MODULE_MANAGER.getModuleT(LagNotifier::class.java)
        if (lagNotifier.takeoffPaused) {
            mc.timer.tickLength = 50.0f
            return
        }
        if (easyTakeOff.value && !mc.player.onGround && mc.player.motionY < -0.04) {
            if (timerControl.value) mc.timer.tickLength = 200.0f
            mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
            hoverTarget = mc.player.posY + 0.2
        } else if (mc.player.onGround) mc.timer.tickLength = 50.0f /* Reset timer if player is on ground */
    }

    /* Calculate yaw for control and packet mode */
    private fun getYaw(): Double {
        var strafeYawDeg = 90.0f * mc.player.movementInput.moveStrafe
        strafeYawDeg *= if (mc.player.movementInput.moveForward != 0.0f) mc.player.movementInput.moveForward * 0.5f else 1.0f
        var yawDeg = mc.player.rotationYaw - strafeYawDeg

        yawDeg -= if (mc.player.movementInput.moveForward < 0.0f) 180 else 0
        packetYaw = yawDeg

        return Math.toRadians(yawDeg.toDouble())
    }

    /* End of Generic Functions */

    /* Boost mode */
    private fun boostMode() {
        val yaw = Math.toRadians(mc.player.rotationYaw.toDouble())
        mc.player.motionX -= mc.player.movementInput.moveForward * sin(yaw) * speedBoost.value / 20
        if (mc.player.movementInput.jump) mc.player.motionY += upSpeedBoost.value / 15 else if (mc.player.movementInput.sneak) mc.player.motionY -= downSpeedBoost.value / 15
        mc.player.motionZ += mc.player.movementInput.moveForward * cos(yaw) * speedBoost.value / 20
    }

    /* Control Mode */
    private fun controlMode(event: PlayerTravelEvent) {
        // TODO: Remove leg twitching when standing still

        /* States and movement input */
        val currentSpeed = sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)
        val inventoryMove = MODULE_MANAGER.getModuleT(InventoryMove::class.java)
        val moveUp = mc.player.movementInput.jump
        val moveDown = if (inventoryMove.isEnabled && !inventoryMove.sneak.value && mc.currentScreen != null || moveUp) false else mc.player.movementInput.sneak
        val yaw = getYaw()

        /* Dynamic down speed */
        val calcDownSpeed = if (dynamicDownSpeed.value) {
            val min = minDownSpeedControl.value.toDouble()
            val max = maxDownSpeedControl.value.toDouble()
            if (mc.player.rotationPitch > 0) {
                mc.player.rotationPitch / 90.0 * (min.coerceAtLeast(max) - min.coerceAtMost(max)) + min.coerceAtMost(max)
            } else minDownSpeedControl.value.toDouble()
        } else downSpeedControl.value.toDouble()

        /* Hover */
        if (hoverTarget < 0.0 || moveUp) hoverTarget = mc.player.posY else if (moveDown) hoverTarget = mc.player.posY - calcDownSpeed
        hoverState = (if (hoverState) mc.player.posY < hoverTarget else mc.player.posY < hoverTarget - 0.1) && altitudeHoldControl.value

        /* Set velocity */
        if (!isStandingStillH || moveUp) {
            if ((moveUp || hoverState) && (currentSpeed >= 0.8 || mc.player.motionY > 1.0)) {
                upwardFlight(currentSpeed, yaw)
            } else if (!isStandingStillH || moveUp) { /* Runs when pressing wasd */
                packetPitch = forwardPitch.value.toFloat()
                mc.player.motionX = sin(-yaw) * speedControl.value
                mc.player.motionY = -fallSpeedControl.value.toDouble()
                mc.player.motionZ = cos(yaw) * speedControl.value
            }
        } else mc.player.setVelocity(0.0, 0.0, 0.0) /* Stop moving if no inputs are pressed */

        if (moveDown) mc.player.motionY = -calcDownSpeed /* Runs when holding shift */

        event.cancel()
    }

    private fun upwardFlight(currentSpeed: Double, yaw: Double) {
        /* Smooth the boost pitch to bypass the pitch limit */
        val targetPitch = (mc.player.rotationPitch * (90.0f - boostPitchControl.value.toFloat()) / 90.0f - boostPitchControl.value.toFloat()).coerceAtLeast(-90.0f)
        packetPitch = if (mc.player.rotationPitch < 0.0f) {
            if (packetPitch > -boostPitchControl.value.toFloat()) -boostPitchControl.value.toFloat() else {
                if (packetPitch < targetPitch) packetPitch += 15.0f
                if (packetPitch > targetPitch) packetPitch -= 15.0f
                packetPitch.coerceAtLeast(targetPitch)
            }
        } else -boostPitchControl.value.toFloat()

        /* These are actually the original Minecraft elytra fly code lol */
        val pitch = Math.toRadians(packetPitch.toDouble())
        val targetMotionX = sin(-yaw) * sin(-pitch)
        val targetMotionZ = cos(yaw) * sin(-pitch)
        val targetSpeed = sqrt(targetMotionX * targetMotionX + targetMotionZ * targetMotionZ)
        val upSpeed = currentSpeed * sin(-pitch) * 0.04
        val fallSpeed = cos(pitch) * cos(pitch) * 0.06 - 0.08

        mc.player.motionX -= upSpeed * targetMotionX / targetSpeed - (targetMotionX / targetSpeed * currentSpeed - mc.player.motionX) * 0.1
        mc.player.motionY += upSpeed * 3.2 + fallSpeed
        mc.player.motionZ -= upSpeed * targetMotionZ / targetSpeed - (targetMotionZ / targetSpeed * currentSpeed - mc.player.motionZ) * 0.1

        /* Passive motion loss */
        mc.player.motionX *= 0.99
        mc.player.motionY *= 0.98
        mc.player.motionZ *= 0.99
    }
    /* End of Control Mode */

    /* Creative Mode */
    private fun creativeMode() {
        if (mc.player.onGround) {
            mc.player.capabilities.isFlying = false
            return
        }

        packetPitch = forwardPitch.value.toFloat()
        mc.player.capabilities.isFlying = true
        mc.player.capabilities.flySpeed = speedCreative.value

        val motionY = when {
            isStandingStill -> 0.0
            mc.player.movementInput.jump -> upSpeedCreative.value.toDouble()
            mc.player.movementInput.sneak -> -downSpeedCreative.value.toDouble()
            else -> -fallSpeedCreative.value.toDouble()
        }
        mc.player.setVelocity(0.0, motionY, 0.0) /* Remove the creative flight acceleration and set the motionY */
    }

    /* Packet Mode */
    private fun packetMode(event: PlayerTravelEvent) {
        isPacketFlying = !mc.player.onGround
        mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
        val yaw = getYaw()

        /* Set velocity */
        if (!isStandingStillH) { /* Runs when pressing wasd */
            mc.player.motionX = sin(-yaw) * speedPacket.value
            mc.player.motionZ = cos(yaw) * speedPacket.value
        } else mc.player.setVelocity(0.0, 0.0, 0.0)

        mc.player.motionY = (if (mc.player.movementInput.sneak) -downSpeedPacket.value else -fallSpeedPacket.value).toDouble()

        event.cancel()
    }

    override fun onUpdate() {
        /* Continuously update server side rotation */
        if (mode.value != ElytraFlightMode.BOOST && isFlying && spoofPitch.value) {
            mc.player.rotationYaw += random().toFloat() * 0.01f - 0.005f
            mc.player.rotationPitch += random().toFloat() * 0.01f - 0.005f
        }
    }

    override fun onDisable() {
        isPacketFlying = false
        mc.timer.tickLength = 50.0f
        mc.player.capabilities.flySpeed = 0.05f
        mc.player.capabilities.isFlying = false
    }

    override fun onEnable() {
        hoverTarget = -1.0 /* For control mode */
    }

    private fun defaults() {
        mc.player?.let {
            durabilityWarning.value = true
            threshold.value = 5

            easyTakeOff.value = true
            timerControl.value = true

            spoofPitch.value = true
            forwardPitch.value = 0

            speedBoost.value = 1.0f
            upSpeedBoost.value = 1.0f
            downSpeedBoost.value = 1.0f

            boostPitchControl.value = 20
            altitudeHoldControl.value = false
            dynamicDownSpeed.value = false
            speedControl.value = 1.81f
            fallSpeedControl.value = 0.00000000000003f
            downSpeedControl.value = 1.0f
            minDownSpeedControl.value = 0.5f
            maxDownSpeedControl.value = 2.0f

            speedCreative.value = 1.8f
            fallSpeedCreative.value = 0.00001f
            upSpeedCreative.value = 1.0f
            downSpeedCreative.value = 1.0f

            speedPacket.value = 1.8f
            fallSpeedPacket.value = 0.00001f
            downSpeedPacket.value = 1.0f

            defaultSetting.value = false
            sendChatMessage("$chatName Set to defaults!")
            closeSettings()
        }
    }

    init {
        defaultSetting.settingListener = SettingListeners { if (defaultSetting.value) defaults() }
    }
}