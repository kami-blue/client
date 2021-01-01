package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.extension.rotationPitch
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.LagNotifier
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.WorldUtils.getGroundPos
import me.zeroeightsix.kami.util.WorldUtils.isLiquidBelow
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketPlayerPosLook
import org.kamiblue.event.listener.listener
import kotlin.math.*

@Module.Info(
        name = "ElytraFlight",
        description = "Allows infinite and way easier Elytra flying",
        category = Module.Category.MOVEMENT,
        modulePriority = 1000
)
object ElytraFlight : Module() {
    private val mode = register(Settings.enumBuilder(ElytraFlightMode::class.java).withName("Mode").withValue(ElytraFlightMode.CONTROL))
    private val page = register(Settings.e<Page>("Page", Page.GENERIC_SETTINGS))
    private val durabilityWarning = register(Settings.booleanBuilder("DurabilityWarning").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS })
    private val threshold = register(Settings.integerBuilder("Broken%").withValue(5).withRange(1, 50).withStep(1).withVisibility { durabilityWarning.value && page.value == Page.GENERIC_SETTINGS })
    private val autoLanding = register(Settings.booleanBuilder("AutoLanding").withValue(false).withVisibility { page.value == Page.GENERIC_SETTINGS })

    /* Generic Settings */
    /* Takeoff */
    private val easyTakeOff = register(Settings.booleanBuilder("EasyTakeoff").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS })
    private val timerControl = register(Settings.booleanBuilder("TakeoffTimer").withValue(true).withVisibility { easyTakeOff.value && page.value == Page.GENERIC_SETTINGS })
    private val highPingOptimize = register(Settings.booleanBuilder("HighPingOptimize").withValue(false).withVisibility { easyTakeOff.value && page.value == Page.GENERIC_SETTINGS })
    private val minTakeoffHeight = register(Settings.floatBuilder("MinTakeoffHeight").withValue(0.5f).withRange(0.0f, 1.5f).withStep(0.1f).withVisibility { easyTakeOff.value && !highPingOptimize.value && page.value == Page.GENERIC_SETTINGS })

    /* Acceleration */
    private val accelerateStartSpeed = register(Settings.integerBuilder("StartSpeed").withValue(100).withRange(0, 100).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val accelerateTime = register(Settings.floatBuilder("AccelerateTime").withValue(0.0f).withRange(0.0f, 10.0f).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val autoReset = register(Settings.booleanBuilder("AutoReset").withValue(false).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })

    /* Spoof Pitch */
    private val spoofPitch = register(Settings.booleanBuilder("SpoofPitch").withValue(true).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val blockInteract = register(Settings.booleanBuilder("BlockInteract").withValue(false).withVisibility { spoofPitch.value && mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })
    private val forwardPitch = register(Settings.integerBuilder("ForwardPitch").withValue(0).withRange(-90, 90).withStep(5).withVisibility { spoofPitch.value && mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS })

    /* Extra */
    val elytraSounds: Setting<Boolean> = register(Settings.booleanBuilder("ElytraSounds").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS })
    private val swingSpeed = register(Settings.floatBuilder("SwingSpeed").withValue(1.0f).withRange(0.0f, 2.0f).withVisibility { page.value == Page.GENERIC_SETTINGS && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET) })
    private val swingAmount = register(Settings.floatBuilder("SwingAmount").withValue(0.8f).withRange(0.0f, 2.0f).withVisibility { page.value == Page.GENERIC_SETTINGS && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET) })
    /* End of Generic Settings */

    /* Mode Settings */
    /* Boost */
    private val speedBoost = register(Settings.floatBuilder("SpeedB").withValue(1.0f).withRange(0.0f, 10.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS })
    private val upSpeedBoost = register(Settings.floatBuilder("UpSpeedB").withValue(1.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS })
    private val downSpeedBoost = register(Settings.floatBuilder("DownSpeedB").withValue(1.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS })

    /* Control */
    private val boostPitchControl = register(Settings.integerBuilder("BaseBoostPitch").withValue(20).withRange(0, 90).withStep(5).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val ncpStrict = register(Settings.booleanBuilder("NCPStrict").withValue(true).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val legacyLookBoost = register(Settings.booleanBuilder("LegacyLookBoost").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val altitudeHoldControl = register(Settings.booleanBuilder("AutoControlAltitude").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val dynamicDownSpeed = register(Settings.booleanBuilder("DynamicDownSpeed").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val speedControl = register(Settings.floatBuilder("SpeedC").withValue(1.81f).withRange(0.0f, 10.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val fallSpeedControl = register(Settings.floatBuilder("FallSpeedC").withValue(0.00000000000003f).withRange(0.0f, 0.3f).withStep(0.01f).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val downSpeedControl = register(Settings.floatBuilder("DownSpeedC").withValue(1.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS })
    private val fastDownSpeedControl = register(Settings.floatBuilder("DynamicDownSpeedC").withValue(2.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.CONTROL && dynamicDownSpeed.value && page.value == Page.MODE_SETTINGS })

    /* Creative */
    private val speedCreative = register(Settings.floatBuilder("SpeedCR").withValue(1.8f).withRange(0.0f, 10.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })
    private val fallSpeedCreative = register(Settings.floatBuilder("FallSpeedCR").withValue(0.00001f).withRange(0.0f, 0.3f).withStep(0.01f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })
    private val upSpeedCreative = register(Settings.floatBuilder("UpSpeedCR").withValue(1.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })
    private val downSpeedCreative = register(Settings.floatBuilder("DownSpeedCR").withValue(1.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS })

    /* Packet */
    private val speedPacket = register(Settings.floatBuilder("SpeedP").withValue(1.8f).withRange(0.0f, 10.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS })
    private val fallSpeedPacket = register(Settings.floatBuilder("FallSpeedP").withValue(0.00001f).withRange(0.0f, 0.3f).withStep(0.01f).withVisibility { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS })
    private val downSpeedPacket = register(Settings.floatBuilder("DownSpeedP").withValue(1.0f).withRange(1.0f, 5.0f).withStep(0.1f).withVisibility { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS })
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
        listener<PacketEvent.Receive> {
            if (mc.player == null || mc.player.isSpectator || !elytraIsEquipped || elytraDurability <= 1 || !isFlying || mode.value == ElytraFlightMode.BOOST) return@listener
            if (it.packet is SPacketPlayerPosLook && mode.value != ElytraFlightMode.PACKET) {
                val packet = it.packet
                packet.rotationPitch = mc.player.rotationPitch
            }

            /* Cancels the elytra opening animation */
            if (it.packet is SPacketEntityMetadata && isPacketFlying) {
                val packet = it.packet
                if (packet.entityId == mc.player.entityId) it.cancel()
            }
        }


        listener<PlayerTravelEvent> {
            if (mc.player == null || mc.player.isSpectator) return@listener
            stateUpdate(it)
            if (elytraIsEquipped && elytraDurability > 1) {
                if (autoLanding.value) {
                    landing(it)
                    return@listener
                }
                if (!isFlying && !isPacketFlying) {
                    takeoff(it)
                } else {
                    mc.timer.tickLength = 50.0f
                    mc.player.isSprinting = false
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
    private fun stateUpdate(event: PlayerTravelEvent) {
        /* Elytra Check */
        val armorSlot = mc.player.inventory.armorInventory[2]
        elytraIsEquipped = armorSlot.item == Items.ELYTRA

        /* Elytra Durability Check */
        if (elytraIsEquipped) {
            val oldDurability = elytraDurability
            elytraDurability = armorSlot.maxDamage - armorSlot.itemDamage

            /* Elytra Durability Warning, runs when player is in the air and durability changed */
            if (!mc.player.onGround && oldDurability != elytraDurability) {
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
        if (!mc.player.onGround && elytraDurability <= 1 && outOfDurability) {
            holdPlayer(event)
        } else if (outOfDurability) outOfDurability = false /* Reset if players is on ground or replace with a new elytra */

        /* wasInLiquid check */
        if (mc.player.isInWater || mc.player.isInLava) {
            wasInLiquid = true
        } else if (mc.player.onGround || isFlying || isPacketFlying) {
            wasInLiquid = false
        }

        /* Elytra flying status check */
        isFlying = mc.player.isElytraFlying || (mc.player.capabilities.isFlying && mode.value == ElytraFlightMode.CREATIVE)

        /* Movement input check */
        isStandingStillH = mc.player.movementInput.moveForward == 0f && mc.player.movementInput.moveStrafe == 0f
        isStandingStill = isStandingStillH && !mc.player.movementInput.jump && !mc.player.movementInput.sneak

        /* Reset acceleration */
        if (!isFlying || isStandingStill) speedPercentage = accelerateStartSpeed.value.toFloat()

        /* Modify leg swing */
        if (shouldSwing()) {
            mc.player.prevLimbSwingAmount = mc.player.limbSwingAmount
            mc.player.limbSwing += swingSpeed.value
            val speedRatio = (mc.player.speed / getSettingSpeed()).toFloat()
            mc.player.limbSwingAmount += ((speedRatio * swingAmount.value) - mc.player.limbSwingAmount) * 0.4f
        }
    }

    private fun reset(cancelFlying: Boolean) {
        wasInLiquid = false
        isFlying = false
        isPacketFlying = false
        if (mc.player != null) {
            mc.timer.tickLength = 50.0f
            mc.player.capabilities.flySpeed = 0.05f
            if (cancelFlying) mc.player.capabilities.isFlying = false
        }
    }

    /* Holds player in the air */
    private fun holdPlayer(event: PlayerTravelEvent) {
        event.cancel()
        mc.timer.tickLength = 50.0f
        mc.player.setVelocity(0.0, -0.01, 0.0)
    }

    /* Auto landing */
    private fun landing(event: PlayerTravelEvent) {
        when {
            mc.player.onGround -> {
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
            mc.player.capabilities.isFlying || !mc.player.isElytraFlying || isPacketFlying -> {
                reset(true)
                takeoff(event)
                return
            }
            else -> {
                when {
                    mc.player.posY > getGroundPos().y + 1.0 -> {
                        mc.timer.tickLength = 50.0f
                        mc.player.motionY = max(min(-(mc.player.posY - getGroundPos().y) / 20.0, -0.5), -5.0)
                    }
                    mc.player.motionY != 0.0 -> { /* Pause falling to reset fall distance */
                        if (!mc.isSingleplayer) mc.timer.tickLength = 200.0f /* Use timer to pause longer */
                        mc.player.motionY = 0.0
                    }
                    else -> {
                        mc.player.motionY = -0.2
                    }
                }
            }
        }
        mc.player.setVelocity(0.0, mc.player.motionY, 0.0) /* Kills horizontal motion */
        event.cancel()
    }

    /* The best takeoff method <3 */
    private fun takeoff(event: PlayerTravelEvent) {
        /* Pause Takeoff if server is lagging, player is in water/lava, or player is on ground */
        val timerSpeed = if (highPingOptimize.value) 400.0f else 200.0f
        val height = if (highPingOptimize.value) 0.0f else minTakeoffHeight.value
        val closeToGround = mc.player.posY <= getGroundPos().y + height && !wasInLiquid && !mc.isSingleplayer
        if (!easyTakeOff.value || (LagNotifier.paused && LagNotifier.pauseTakeoff.value)|| mc.player.onGround) {
            if (LagNotifier.paused && LagNotifier.pauseTakeoff.value && mc.player.posY - getGroundPos().y > 4.0f) holdPlayer(event) /* Holds player in the air if server is lagging and the distance is enough for taking fall damage */
            reset(mc.player.onGround)
            return
        }
        if (mc.player.motionY < 0 && !highPingOptimize.value || mc.player.motionY < -0.02) {
            if (closeToGround) {
                mc.timer.tickLength = 25.0f
                return
            }
            if (!highPingOptimize.value && !wasInLiquid && !mc.isSingleplayer) { /* Cringe moment when you use elytra flight in single player world */
                event.cancel()
                mc.player.setVelocity(0.0, -0.02, 0.0)
            }
            if (timerControl.value && !mc.isSingleplayer) mc.timer.tickLength = timerSpeed * 2.0f
            mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
            hoverTarget = mc.player.posY + 0.2
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
            else -> 0.0f
        }
    }

    private fun setSpeed(yaw: Double, boosting: Boolean) {
        val acceleratedSpeed = getSpeed(boosting)
        mc.player.setVelocity(sin(-yaw) * acceleratedSpeed, mc.player.motionY, cos(yaw) * acceleratedSpeed)
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
        /* States and movement input */
        val currentSpeed = sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)
        val moveUp = if (!legacyLookBoost.value) mc.player.movementInput.jump else mc.player.rotationPitch < -10.0f && !isStandingStillH
        val moveDown = if (InventoryMove.isEnabled && !InventoryMove.sneak.value && mc.currentScreen != null || moveUp) false else mc.player.movementInput.sneak

        /* Dynamic down speed */
        val calcDownSpeed = if (dynamicDownSpeed.value) {
            val minDownSpeed = min(downSpeedControl.value, fastDownSpeedControl.value).toDouble()
            val maxDownSpeed = max(downSpeedControl.value, fastDownSpeedControl.value).toDouble()
            if (mc.player.rotationPitch > 0) {
                mc.player.rotationPitch / 90.0 * (maxDownSpeed - minDownSpeed) + minDownSpeed
            } else minDownSpeed
        } else downSpeedControl.value.toDouble()

        /* Hover */
        if (hoverTarget < 0.0 || moveUp) hoverTarget = mc.player.posY else if (moveDown) hoverTarget = mc.player.posY - calcDownSpeed
        hoverState = (if (hoverState) mc.player.posY < hoverTarget else mc.player.posY < hoverTarget - 0.1) && altitudeHoldControl.value

        /* Set velocity */
        if (!isStandingStillH || moveUp) {
            if ((moveUp || hoverState) && (currentSpeed >= 0.8 || mc.player.motionY > 1.0)) {
                upwardFlight(currentSpeed, getYaw())
            } else if (!isStandingStillH || moveUp) { /* Runs when pressing wasd */
                packetPitch = forwardPitch.value.toFloat()
                mc.player.motionY = -fallSpeedControl.value.toDouble()
                setSpeed(getYaw(), moveUp)
                boostingTick = 0
            }
        } else mc.player.setVelocity(0.0, 0.0, 0.0) /* Stop moving if no inputs are pressed */

        if (moveDown) mc.player.motionY = -calcDownSpeed /* Runs when holding shift */

        event.cancel()
    }

    private fun upwardFlight(currentSpeed: Double, yaw: Double) {
        val multipliedSpeed = 0.128 * min(speedControl.value, 2.0f)
        val strictPitch = Math.toDegrees(asin((multipliedSpeed - sqrt(multipliedSpeed * multipliedSpeed - 0.0348)) / 0.12)).toFloat()
        val basePitch = if (ncpStrict.value && strictPitch < boostPitchControl.value && !strictPitch.isNaN()) -strictPitch
        else -boostPitchControl.value.toFloat()
        val targetPitch = if (mc.player.rotationPitch < 0.0f) {
            max(mc.player.rotationPitch * (90.0f - boostPitchControl.value.toFloat()) / 90.0f - boostPitchControl.value.toFloat(), -90.0f)
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
            reset(true)
            return
        }

        packetPitch = forwardPitch.value.toFloat()
        mc.player.capabilities.isFlying = true
        mc.player.capabilities.flySpeed = getSpeed(false).toFloat()

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

        /* Set velocity */
        if (!isStandingStillH) { /* Runs when pressing wasd */
            setSpeed(getYaw(), false)
        } else mc.player.setVelocity(0.0, 0.0, 0.0)
        mc.player.motionY = (if (mc.player.movementInput.sneak) -downSpeedPacket.value else -fallSpeedPacket.value).toDouble()

        event.cancel()
    }

    fun shouldSwing(): Boolean {
        return isEnabled && isFlying && !autoLanding.value && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET)
    }

    private fun spoofRotation() {
        if (mc.player.isSpectator || !elytraIsEquipped || elytraDurability <= 1 || !isFlying) return
        val packet = PlayerPacketManager.PlayerPacket(rotating = true)
        var rotation = Vec2f(mc.player)

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
        PlayerPacketManager.addPacket(this, packet)
    }

    override fun onDisable() {
        reset(true)
    }

    override fun onEnable() {
        autoLanding.value = false
        speedPercentage = accelerateStartSpeed.value.toFloat() /* For acceleration */
        hoverTarget = -1.0 /* For control mode */
    }

    init {
        /* Reset isFlying states when switching mode */
        mode.settingListener = SettingListeners {
            reset(true)
        }
    }
}
