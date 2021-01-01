package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.extension.rotationPitch
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.LagNotifier
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.WorldUtils.getGroundPos
import me.zeroeightsix.kami.util.WorldUtils.isLiquidBelow
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketPlayerPosLook
import org.kamiblue.commons.extension.toRadian
import kotlin.math.*

@Module.Info(
    name = "ElytraFlight",
    description = "Allows infinite and way easier Elytra flying",
    category = Module.Category.MOVEMENT,
    modulePriority = 1000
)
object ElytraFlight : Module() {
    private val mode = setting("Mode", ElytraFlightMode.CONTROL)
    private val page = setting("Page", Page.GENERIC_SETTINGS)
    private val durabilityWarning = setting("DurabilityWarning", true, { page.value == Page.GENERIC_SETTINGS })
    private val threshold = setting("Broken%", 5, 1..50, 1, { durabilityWarning.value && page.value == Page.GENERIC_SETTINGS })
    private val autoLanding = setting("AutoLanding", false, { page.value == Page.GENERIC_SETTINGS })

    /* Generic Settings */
    /* Takeoff */
    private val easyTakeOff = setting("EasyTakeoff", true, { page.value == Page.GENERIC_SETTINGS })
    private val timerControl = setting("TakeoffTimer", true, { easyTakeOff.value && page.value == Page.GENERIC_SETTINGS })
    private val highPingOptimize = setting("HighPingOptimize", false, { easyTakeOff.value && page.value == Page.GENERIC_SETTINGS })
    private val minTakeoffHeight = setting("MinTakeoffHeight", 0.5f, 0.0f..1.5f, 0.1f, { easyTakeOff.value && !highPingOptimize.value && page.value == Page.GENERIC_SETTINGS })

    /* Acceleration */
    private val accelerateStartSpeed = setting("StartSpeed", 100, 0..100, 5, { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val accelerateTime = setting("AccelerateTime", 0.0f, 0.0f..10.0f, 0.25f, { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val autoReset = setting("AutoReset", false, { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })

    /* Spoof Pitch */
    private val spoofPitch = setting("SpoofPitch", true, { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val blockInteract = setting("BlockInteract", false, { spoofPitch.value && mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val forwardPitch = setting("ForwardPitch", 0, -90..90, 5, { spoofPitch.value && mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })

    /* Extra */
    val elytraSounds = setting("ElytraSounds", true, { page.value == Page.GENERIC_SETTINGS })
    private val swingSpeed = setting("SwingSpeed", 1.0f, 0.0f..2.0f, 0.1f, { page.value == Page.GENERIC_SETTINGS && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET) })
    private val swingAmount = setting("SwingAmount", 0.8f, 0.0f..2.0f, 0.1f, { page.value == Page.GENERIC_SETTINGS && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET) })
    /* End of Generic Settings */

    /* Mode Settings */
    /* Boost */
    private val speedBoost = setting("SpeedB", 1.0f, 0.0f..10.0f, 0.1f, { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS })
    private val upSpeedBoost = setting("UpSpeedB", 1.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS })
    private val downSpeedBoost = setting("DownSpeedB", 1.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS })

    /* Control */
    private val boostPitchControl = setting("BaseBoostPitch", 20, 0..90, 5, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val ncpStrict = setting("NCPStrict", true, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val legacyLookBoost = setting("LegacyLookBoost", false, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val altitudeHoldControl = setting("AutoControlAltitude", false, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val dynamicDownSpeed = setting("DynamicDownSpeed", false, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val speedControl = setting("SpeedC", 1.81f, 0.0f..10.0f, 0.1f, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val fallSpeedControl = setting("FallSpeedC", 0.00000000000003f, 0.0f..0.3f, 0.01f, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val downSpeedControl = setting("DownSpeedC", 1.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val fastDownSpeedControl = setting("DynamicDownSpeedC", 2.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.CONTROL && dynamicDownSpeed.value && page.value == Page.MODE_SETTINGS })

    /* Creative */
    private val speedCreative = setting("SpeedCR", 1.8f, 0.0f..10.0f, 0.1f, { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })
    private val fallSpeedCreative = setting("FallSpeedCR", 0.00001f, 0.0f..0.3f, 0.01f, { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })
    private val upSpeedCreative = setting("UpSpeedCR", 1.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })
    private val downSpeedCreative = setting("DownSpeedCR", 1.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })

    /* Packet */
    private val speedPacket = setting("SpeedP", 1.8f, 0.0f..10.0f, 0.1f, { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS })
    private val fallSpeedPacket = setting("FallSpeedP", 0.00001f, 0.0f..0.3f, 0.01f, { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS })
    private val downSpeedPacket = setting("DownSpeedP", 1.0f, 1.0f..5.0f, 0.1f, { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS })
    /* End of Mode Settings */

    private enum class ElytraFlightMode {
        BOOST, CONTROL, CREATIVE, PACKET
    }

    private enum class Page {
        GENERIC_SETTINGS, MODE_SETTINGS
    }

    /* Generic states */
    private var elytraIsEquipped = false
    private var elytraDurability = 0
    private var outOfDurability = false
    private var wasInLiquid = false
    var isFlying = false
    private var isPacketFlying = false
    private var isStandingStillH = false
    private var isStandingStill = false
    private var speedPercentage = 0.0f

    /* Control mode states */
    private var hoverTarget = -1.0
    private var packetYaw = 0.0f
    private var packetPitch = 0.0f
    private var hoverState = false
    private var boostingTick = 0

    /* Event Listeners */
    init {
        safeListener<PacketEvent.Receive> {
            if (player.isSpectator || !elytraIsEquipped || elytraDurability <= 1 || !isFlying || mode.value == ElytraFlightMode.BOOST) return@safeListener
            if (it.packet is SPacketPlayerPosLook && mode.value != ElytraFlightMode.PACKET) {
                val packet = it.packet
                packet.rotationPitch = player.rotationPitch
            }

            /* Cancels the elytra opening animation */
            if (it.packet is SPacketEntityMetadata && isPacketFlying) {
                val packet = it.packet
                if (packet.entityId == player.entityId) it.cancel()
            }
        }

        safeListener<PlayerTravelEvent> {
            if (player.isSpectator) return@safeListener
            stateUpdate(it)
            if (elytraIsEquipped && elytraDurability > 1) {
                if (autoLanding.value) {
                    landing(it)
                    return@safeListener
                }
                if (!isFlying && !isPacketFlying) {
                    takeoff(it)
                } else {
                    mc.timer.tickLength = 50.0f
                    player.isSprinting = false
                    when (mode.value) {
                        ElytraFlightMode.BOOST -> boostMode()
                        ElytraFlightMode.CONTROL -> controlMode(it)
                        ElytraFlightMode.CREATIVE -> creativeMode()
                        ElytraFlightMode.PACKET -> packetMode(it)
                    }
                }
                spoofRotation()
            } else if (!outOfDurability) {
                reset(true)
            }
        }
    }
    /* End of Event Listeners */

    /* Generic Functions */
    private fun SafeClientEvent.stateUpdate(event: PlayerTravelEvent) {
        /* Elytra Check */
        val armorSlot = player.inventory.armorInventory[2]
        elytraIsEquipped = armorSlot.item == Items.ELYTRA

        /* Elytra Durability Check */
        if (elytraIsEquipped) {
            val oldDurability = elytraDurability
            elytraDurability = armorSlot.maxDamage - armorSlot.itemDamage

            /* Elytra Durability Warning, runs when player is in the air and durability changed */
            if (!player.onGround && oldDurability != elytraDurability) {
                if (durabilityWarning.value && elytraDurability > 1 && elytraDurability < threshold.value * armorSlot.maxDamage / 100) {
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    sendChatMessage("$chatName Warning: Elytra has " + (elytraDurability - 1) + " durability remaining")
                } else if (elytraDurability <= 1 && !outOfDurability) {
                    outOfDurability = true
                    if (durabilityWarning.value) {
                        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                        sendChatMessage("$chatName Elytra is out of durability, holding player in the air")
                    }
                }
            }
        } else elytraDurability = 0

        /* Holds player in the air if run out of durability */
        if (!player.onGround && elytraDurability <= 1 && outOfDurability) {
            holdPlayer(event)
        } else if (outOfDurability) outOfDurability = false /* Reset if players is on ground or replace with a new elytra */

        /* wasInLiquid check */
        if (player.isInWater || player.isInLava) {
            wasInLiquid = true
        } else if (player.onGround || isFlying || isPacketFlying) {
            wasInLiquid = false
        }

        /* Elytra flying status check */
        isFlying = player.isElytraFlying || (player.capabilities.isFlying && mode.value == ElytraFlightMode.CREATIVE)

        /* Movement input check */
        isStandingStillH = player.movementInput.moveForward == 0f && player.movementInput.moveStrafe == 0f
        isStandingStill = isStandingStillH && !player.movementInput.jump && !player.movementInput.sneak

        /* Reset acceleration */
        if (!isFlying || isStandingStill) speedPercentage = accelerateStartSpeed.value.toFloat()

        /* Modify leg swing */
        if (shouldSwing()) {
            player.prevLimbSwingAmount = player.limbSwingAmount
            player.limbSwing += swingSpeed.value
            val speedRatio = (player.speed / getSettingSpeed()).toFloat()
            player.limbSwingAmount += ((speedRatio * swingAmount.value) - player.limbSwingAmount) * 0.4f
        }
    }

    private fun SafeClientEvent.reset(cancelFlying: Boolean) {
        wasInLiquid = false
        isFlying = false
        isPacketFlying = false
        mc.timer.tickLength = 50.0f
        player.capabilities.flySpeed = 0.05f
        if (cancelFlying) player.capabilities.isFlying = false
    }

    /* Holds player in the air */
    private fun SafeClientEvent.holdPlayer(event: PlayerTravelEvent) {
        event.cancel()
        mc.timer.tickLength = 50.0f
        player.setVelocity(0.0, -0.01, 0.0)
    }

    /* Auto landing */
    private fun SafeClientEvent.landing(event: PlayerTravelEvent) {
        when {
            player.onGround -> {
                sendChatMessage("$chatName Landed!")
                autoLanding.value = false
                return
            }
            isLiquidBelow() -> {
                sendChatMessage("$chatName Liquid below, disabling.")
                autoLanding.value = false
            }
            LagNotifier.paused && LagNotifier.pauseTakeoff.value -> {
                holdPlayer(event)
            }
            player.capabilities.isFlying || !player.isElytraFlying || isPacketFlying -> {
                reset(true)
                takeoff(event)
                return
            }
            else -> {
                when {
                    player.posY > getGroundPos().y + 1.0 -> {
                        mc.timer.tickLength = 50.0f
                        player.motionY = max(min(-(player.posY - getGroundPos().y) / 20.0, -0.5), -5.0)
                    }
                    player.motionY != 0.0 -> { /* Pause falling to reset fall distance */
                        if (!mc.isSingleplayer) mc.timer.tickLength = 200.0f /* Use timer to pause longer */
                        player.motionY = 0.0
                    }
                    else -> {
                        player.motionY = -0.2
                    }
                }
            }
        }
        player.setVelocity(0.0, player.motionY, 0.0) /* Kills horizontal motion */
        event.cancel()
    }

    /* The best takeoff method <3 */
    private fun SafeClientEvent.takeoff(event: PlayerTravelEvent) {
        /* Pause Takeoff if server is lagging, player is in water/lava, or player is on ground */
        val timerSpeed = if (highPingOptimize.value) 400.0f else 200.0f
        val height = if (highPingOptimize.value) 0.0f else minTakeoffHeight.value
        val closeToGround = player.posY <= getGroundPos().y + height && !wasInLiquid && !mc.isSingleplayer
        if (!easyTakeOff.value || (LagNotifier.paused && LagNotifier.pauseTakeoff.value) || player.onGround) {
            if (LagNotifier.paused && LagNotifier.pauseTakeoff.value && player.posY - getGroundPos().y > 4.0f) holdPlayer(event) /* Holds player in the air if server is lagging and the distance is enough for taking fall damage */
            reset(player.onGround)
            return
        }
        if (player.motionY < 0 && !highPingOptimize.value || player.motionY < -0.02) {
            if (closeToGround) {
                mc.timer.tickLength = 25.0f
                return
            }
            if (!highPingOptimize.value && !wasInLiquid && !mc.isSingleplayer) { /* Cringe moment when you use elytra flight in single player world */
                event.cancel()
                player.setVelocity(0.0, -0.02, 0.0)
            }
            if (timerControl.value && !mc.isSingleplayer) mc.timer.tickLength = timerSpeed * 2.0f
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))
            hoverTarget = player.posY + 0.2
        } else if (highPingOptimize.value && !closeToGround) {
            mc.timer.tickLength = timerSpeed
        }
    }

    /**
     *  Calculate yaw for control and packet mode
     *
     *  @return Yaw in radians based on player rotation yaw and movement input
     */
    private fun getYaw(): Double {
        val yawRad = MovementUtils.calcMoveYaw()
        packetYaw = Math.toDegrees(yawRad).toFloat()
        return yawRad
    }

    /**
     * Calculate a speed with a non linear acceleration over time
     *
     * @return boostingSpeed if [boosting] is true, else return a accelerated speed.
     */
    private fun getSpeed(boosting: Boolean): Double {
        return when {
            boosting -> (if (ncpStrict.value) min(speedControl.value, 2.0f) else speedControl.value).toDouble()

            accelerateTime.value != 0.0f && accelerateStartSpeed.value != 100 -> {
                speedPercentage = when {
                    mc.gameSettings.keyBindSprint.isKeyDown -> 100.0f
                    autoReset.value && speedPercentage >= 100.0f -> accelerateStartSpeed.value.toFloat()
                    else -> min(speedPercentage + (100.0f - accelerateStartSpeed.value.toFloat()) / (accelerateTime.value * 20), 100.0f)
                }
                getSettingSpeed() * (speedPercentage / 100.0) * (cos((speedPercentage / 100.0) * PI) * -0.5 + 0.5)
            }

            else -> getSettingSpeed().toDouble()
        }
    }

    private fun getSettingSpeed(): Float {
        return when (mode.value) {
            ElytraFlightMode.BOOST -> speedBoost.value
            ElytraFlightMode.CONTROL -> speedControl.value
            ElytraFlightMode.CREATIVE -> speedCreative.value
            ElytraFlightMode.PACKET -> speedPacket.value
        }
    }

    private fun SafeClientEvent.setSpeed(yaw: Double, boosting: Boolean) {
        val acceleratedSpeed = getSpeed(boosting)
        player.setVelocity(sin(-yaw) * acceleratedSpeed, player.motionY, cos(yaw) * acceleratedSpeed)
    }
    /* End of Generic Functions */

    /* Boost mode */
    private fun SafeClientEvent.boostMode() {
        val yaw = player.rotationYaw.toDouble().toRadian()
        player.motionX -= player.movementInput.moveForward * sin(yaw) * speedBoost.value / 20
        if (player.movementInput.jump) player.motionY += upSpeedBoost.value / 15 else if (player.movementInput.sneak) player.motionY -= downSpeedBoost.value / 15
        player.motionZ += player.movementInput.moveForward * cos(yaw) * speedBoost.value / 20
    }

    /* Control Mode */
    private fun SafeClientEvent.controlMode(event: PlayerTravelEvent) {
        /* States and movement input */
        val currentSpeed = sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ)
        val moveUp = if (!legacyLookBoost.value) player.movementInput.jump else player.rotationPitch < -10.0f && !isStandingStillH
        val moveDown = if (InventoryMove.isEnabled && !InventoryMove.sneak.value && mc.currentScreen != null || moveUp) false else player.movementInput.sneak

        /* Dynamic down speed */
        val calcDownSpeed = if (dynamicDownSpeed.value) {
            val minDownSpeed = min(downSpeedControl.value, fastDownSpeedControl.value).toDouble()
            val maxDownSpeed = max(downSpeedControl.value, fastDownSpeedControl.value).toDouble()
            if (player.rotationPitch > 0) {
                player.rotationPitch / 90.0 * (maxDownSpeed - minDownSpeed) + minDownSpeed
            } else minDownSpeed
        } else downSpeedControl.value.toDouble()

        /* Hover */
        if (hoverTarget < 0.0 || moveUp) hoverTarget = player.posY else if (moveDown) hoverTarget = player.posY - calcDownSpeed
        hoverState = (if (hoverState) player.posY < hoverTarget else player.posY < hoverTarget - 0.1) && altitudeHoldControl.value

        /* Set velocity */
        if (!isStandingStillH || moveUp) {
            if ((moveUp || hoverState) && (currentSpeed >= 0.8 || player.motionY > 1.0)) {
                upwardFlight(currentSpeed, getYaw())
            } else if (!isStandingStillH || moveUp) { /* Runs when pressing wasd */
                packetPitch = forwardPitch.value.toFloat()
                player.motionY = -fallSpeedControl.value.toDouble()
                setSpeed(getYaw(), moveUp)
                boostingTick = 0
            }
        } else player.setVelocity(0.0, 0.0, 0.0) /* Stop moving if no inputs are pressed */

        if (moveDown) player.motionY = -calcDownSpeed /* Runs when holding shift */

        event.cancel()
    }

    private fun SafeClientEvent.upwardFlight(currentSpeed: Double, yaw: Double) {
        val multipliedSpeed = 0.128 * min(speedControl.value, 2.0f)
        val strictPitch = Math.toDegrees(asin((multipliedSpeed - sqrt(multipliedSpeed * multipliedSpeed - 0.0348)) / 0.12)).toFloat()
        val basePitch = if (ncpStrict.value && strictPitch < boostPitchControl.value && !strictPitch.isNaN()) -strictPitch
        else -boostPitchControl.value.toFloat()
        val targetPitch = if (player.rotationPitch < 0.0f) {
            max(player.rotationPitch * (90.0f - boostPitchControl.value.toFloat()) / 90.0f - boostPitchControl.value.toFloat(), -90.0f)
        } else -boostPitchControl.value.toFloat()

        packetPitch = if (packetPitch <= basePitch && boostingTick > 2) {
            if (packetPitch < targetPitch) packetPitch += 17.0f
            if (packetPitch > targetPitch) packetPitch -= 17.0f
            max(packetPitch, targetPitch)
        } else basePitch
        boostingTick++

        /* These are actually the original Minecraft elytra fly code lol */
        val pitch = Math.toRadians(packetPitch.toDouble())
        val targetMotionX = sin(-yaw) * sin(-pitch)
        val targetMotionZ = cos(yaw) * sin(-pitch)
        val targetSpeed = sqrt(targetMotionX * targetMotionX + targetMotionZ * targetMotionZ)
        val upSpeed = currentSpeed * sin(-pitch) * 0.04
        val fallSpeed = cos(pitch) * cos(pitch) * 0.06 - 0.08

        player.motionX -= upSpeed * targetMotionX / targetSpeed - (targetMotionX / targetSpeed * currentSpeed - player.motionX) * 0.1
        player.motionY += upSpeed * 3.2 + fallSpeed
        player.motionZ -= upSpeed * targetMotionZ / targetSpeed - (targetMotionZ / targetSpeed * currentSpeed - player.motionZ) * 0.1

        /* Passive motion loss */
        player.motionX *= 0.99
        player.motionY *= 0.98
        player.motionZ *= 0.99
    }
    /* End of Control Mode */

    /* Creative Mode */
    private fun SafeClientEvent.creativeMode() {
        if (player.onGround) {
            reset(true)
            return
        }

        packetPitch = forwardPitch.value.toFloat()
        player.capabilities.isFlying = true
        player.capabilities.flySpeed = getSpeed(false).toFloat()

        val motionY = when {
            isStandingStill -> 0.0
            player.movementInput.jump -> upSpeedCreative.value.toDouble()
            player.movementInput.sneak -> -downSpeedCreative.value.toDouble()
            else -> -fallSpeedCreative.value.toDouble()
        }
        player.setVelocity(0.0, motionY, 0.0) /* Remove the creative flight acceleration and set the motionY */
    }

    /* Packet Mode */
    private fun SafeClientEvent.packetMode(event: PlayerTravelEvent) {
        isPacketFlying = !player.onGround
        connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))

        /* Set velocity */
        if (!isStandingStillH) { /* Runs when pressing wasd */
            setSpeed(getYaw(), false)
        } else player.setVelocity(0.0, 0.0, 0.0)
        player.motionY = (if (player.movementInput.sneak) -downSpeedPacket.value else -fallSpeedPacket.value).toDouble()

        event.cancel()
    }

    fun shouldSwing(): Boolean {
        return isEnabled && isFlying && !autoLanding.value && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET)
    }

    private fun SafeClientEvent.spoofRotation() {
        if (player.isSpectator || !elytraIsEquipped || elytraDurability <= 1 || !isFlying) return
        val packet = PlayerPacketManager.PlayerPacket(rotating = true)
        var rotation = Vec2f(player)

        if (autoLanding.value) {
            rotation = Vec2f(rotation.x, -20f)
        } else if (mode.value != ElytraFlightMode.BOOST) {
            if (!isStandingStill && mode.value != ElytraFlightMode.CREATIVE) rotation = Vec2f(packetYaw, rotation.y)
            if (spoofPitch.value) {
                if (!isStandingStill) rotation = Vec2f(rotation.x, packetPitch)

                /* Cancels rotation packets if player is not moving and not clicking */
                val cancelRotation = isStandingStill && ((!mc.gameSettings.keyBindUseItem.isKeyDown && !mc.gameSettings.keyBindAttack.isKeyDown && blockInteract.value) || !blockInteract.value)
                if (cancelRotation) {
                    packet.rotating = false
                }
            }
        }

        packet.rotation = rotation
        PlayerPacketManager.addPacket(this@ElytraFlight, packet)
    }

    override fun onDisable() {
        runSafe { reset(true) }
    }

    override fun onEnable() {
        autoLanding.value = false
        speedPercentage = accelerateStartSpeed.value.toFloat() /* For acceleration */
        hoverTarget = -1.0 /* For control mode */
    }

    init {
        /* Reset isFlying states when switching mode */
        mode.listeners.add {
            runSafe { reset(true) }
        }
    }
}
