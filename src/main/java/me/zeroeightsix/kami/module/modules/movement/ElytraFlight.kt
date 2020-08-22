package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod.MODULE_MANAGER
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.LagNotifier
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils.checkForLiquid
import me.zeroeightsix.kami.util.BlockUtils.getGroundPosY
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.MovementUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketPlayerPosLook
import java.lang.Math.random
import kotlin.math.*

/**
 * Created by 086 on 11/04/2018.
 * Updated by Itistheend on 28/12/19.
 * Updated by pNoName on 28/05/20
 * Updated by dominikaaaa on 06/07/20
 * Updated by Xiaro on 08/07/20
 */
@Module.Info(
        name = "ElytraFlight",
        description = "Allows infinite and way easier Elytra flying",
        category = Module.Category.MOVEMENT
)
class ElytraFlight : Module() {
    private val mode = register(Settings.enumBuilder(ElytraFlightMode::class.java).withName("Mode").withValue(ElytraFlightMode.CONTROL).build())
    private val page = register(Settings.e<Page>("Page", Page.GENERIC_SETTINGS))
    private val defaultSetting = register(Settings.b("Defaults", false))
    private val durabilityWarning = register(Settings.booleanBuilder("DurabilityWarning").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val threshold = register(Settings.integerBuilder("Broken%").withRange(1, 50).withValue(5).withVisibility { durabilityWarning.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val autoLanding = register(Settings.booleanBuilder("AutoLanding").withValue(false).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())

    /* Generic Settings */
    /* Takeoff */
    private val easyTakeOff = register(Settings.booleanBuilder("EasyTakeoff").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val timerControl = register(Settings.booleanBuilder("TakeoffTimer").withValue(true).withVisibility { easyTakeOff.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val highPingOptimize = register(Settings.booleanBuilder("HighPingOptimize").withValue(false).withVisibility { easyTakeOff.value && page.value == Page.GENERIC_SETTINGS }.build())
    private val minTakeoffHeight = register(Settings.floatBuilder("MinTakeoffHeight").withRange(0.0f, 1.5f).withValue(0.5f).withVisibility { easyTakeOff.value && !highPingOptimize.value && page.value == Page.GENERIC_SETTINGS }.build())

    /* Acceleration */
    private val accelerateStartSpeed = register(Settings.integerBuilder("StartSpeed").withRange(0, 100).withValue(100).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS }.build())
    private val accelerateTime = register(Settings.floatBuilder("AccelerateTime").withRange(0.0f, 10.0f).withValue(0.0f).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS }.build())
    private val autoReset = register(Settings.booleanBuilder("AutoReset").withValue(false).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS }.build())

    /* Spoof Pitch */
    private val spoofPitch = register(Settings.booleanBuilder("SpoofPitch").withValue(true).withVisibility { mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS }.build())
    private val blockInteract = register(Settings.booleanBuilder("BlockInteract").withValue(false).withVisibility { spoofPitch.value && mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS }.build())
    private val forwardPitch = register(Settings.integerBuilder("ForwardPitch").withRange(-90, 90).withValue(0).withVisibility { spoofPitch.value && mode.value != ElytraFlightMode.BOOST && page.value == Page.GENERIC_SETTINGS }.build())

    /* Extra */
    val elytraSounds: Setting<Boolean> = register(Settings.booleanBuilder("ElytraSounds").withValue(true).withVisibility { page.value == Page.GENERIC_SETTINGS }.build())
    private val swingSpeed = register(Settings.floatBuilder("SwingSpeed").withValue(1.0f).withRange(0.0f, 2.0f).withVisibility { page.value == Page.GENERIC_SETTINGS && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET) }.build())
    private val swingAmount = register(Settings.floatBuilder("SwingAmount").withValue(0.8f).withRange(0.0f, 2.0f).withVisibility { page.value == Page.GENERIC_SETTINGS && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.PACKET) }.build())
    /* End of Generic Settings */

    /* Mode Settings */
    /* Boost */
    private val speedBoost = register(Settings.floatBuilder("SpeedB").withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS }.build())
    private val upSpeedBoost = register(Settings.floatBuilder("UpSpeedB").withMinimum(0.0f).withValue(1.0f).withMaximum(5.0f).withVisibility { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS }.build())
    private val downSpeedBoost = register(Settings.floatBuilder("DownSpeedB").withMinimum(0.0f).withValue(1.0f).withMaximum(5.0f).withVisibility { mode.value == ElytraFlightMode.BOOST && page.value == Page.MODE_SETTINGS }.build())

    /* Control */
    private val boostPitchControl = register(Settings.integerBuilder("BaseBoostPitch").withRange(0, 90).withValue(20).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val ncpStrict = register(Settings.booleanBuilder("NCPStrict").withValue(true).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val legacyLookBoost = register(Settings.booleanBuilder("LegacyLookBoost").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val altitudeHoldControl = register(Settings.booleanBuilder("AltitudeHold").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val dynamicDownSpeed = register(Settings.booleanBuilder("DynamicDownSpeed").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val speedControl = register(Settings.floatBuilder("SpeedC").withMinimum(0.0f).withValue(1.81f).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val fallSpeedControl = register(Settings.floatBuilder("FallSpeedC").withMinimum(0.0f).withMaximum(0.3f).withValue(0.00000000000003f).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val downSpeedControl = register(Settings.floatBuilder("DownSpeedC").withMaximum(5.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL && page.value == Page.MODE_SETTINGS }.build())
    private val fastDownSpeedControl = register(Settings.floatBuilder("DynamicDownSpeedC").withMaximum(5.0f).withMinimum(0.0f).withValue(2.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL && dynamicDownSpeed.value && page.value == Page.MODE_SETTINGS }.build())

    /* Creative */
    private val speedCreative = register(Settings.floatBuilder("SpeedCR").withMinimum(0.0f).withValue(1.8f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS }.build())
    private val fallSpeedCreative = register(Settings.floatBuilder("FallSpeedCR").withMinimum(0.0f).withMaximum(0.3f).withValue(0.00001f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS }.build())
    private val upSpeedCreative = register(Settings.floatBuilder("UpSpeedCR").withMaximum(5.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS }.build())
    private val downSpeedCreative = register(Settings.floatBuilder("DownSpeedCR").withMaximum(5.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CREATIVE && page.value == Page.MODE_SETTINGS }.build())

    /* Packet */
    private val speedPacket = register(Settings.floatBuilder("SpeedP").withMinimum(0.0f).withValue(1.8f).withVisibility { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS }.build())
    private val fallSpeedPacket = register(Settings.floatBuilder("FallSpeedP").withMinimum(0.0f).withMaximum(0.3f).withValue(0.00001f).withVisibility { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS }.build())
    private val downSpeedPacket = register(Settings.floatBuilder("DownSpeedP").withMinimum(0.0f).withMaximum(5.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.PACKET && page.value == Page.MODE_SETTINGS }.build())
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

    /* Event Handlers */
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (mc.player == null || mc.player.isSpectator || !elytraIsEquipped || elytraDurability <= 1 || !isFlying) return@EventHook
        if (event.packet is CPacketPlayer) {
            val packet = event.packet as CPacketPlayer
            if (autoLanding.value) {
                packet.pitch = -20.0f
            } else if (mode.value != ElytraFlightMode.BOOST) {
                if (spoofPitch.value) {
                    /* Cancels rotation packets if player is not moving and not clicking */
                    val cancelPacket = isStandingStill && ((!mc.gameSettings.keyBindUseItem.isKeyDown && !mc.gameSettings.keyBindAttack.isKeyDown && blockInteract.value) || !blockInteract.value)
                    if (event.packet !is CPacketPlayer.Position && cancelPacket) {
                        event.cancel()
                        if (event.packet is CPacketPlayer.PositionRotation) { /* Resend the packet as position packet if it is position & rotation packet */
                            mc.connection!!.sendPacket(CPacketPlayer.Position(packet.x, packet.y, packet.z, packet.onGround))
                        }
                        return@EventHook
                    }

                    if (!isStandingStill) packet.pitch = packetPitch
                }
                if (mode.value != ElytraFlightMode.CREATIVE) packet.yaw = packetYaw
            }
        }
    })

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || mc.player.isSpectator || !elytraIsEquipped || elytraDurability <= 1 || !isFlying || mode.value == ElytraFlightMode.BOOST) return@EventHook
        if (event.packet is SPacketPlayerPosLook && mode.value != ElytraFlightMode.PACKET) {
            val packet = event.packet as SPacketPlayerPosLook
            packet.pitch = mc.player.rotationPitch
        }

        /* Cancels the elytra opening animation */
        if (event.packet is SPacketEntityMetadata && isPacketFlying) {
            val packet = event.packet as SPacketEntityMetadata
            if (packet.entityId == mc.player.getEntityId()) event.cancel()
        }
    })

    @EventHandler
    private val playerTravelListener = Listener(EventHook { event: PlayerTravelEvent ->
        if (mc.player == null || mc.player.isSpectator) return@EventHook
        stateUpdate(event)
        if (elytraIsEquipped && elytraDurability > 1) {
            if (autoLanding.value) {
                landing(event)
                return@EventHook
            }
            if (!isFlying && !isPacketFlying) {
                takeoff(event)
            } else {
                mc.timer.tickLength = 50.0f
                mc.player.isSprinting = false
                when (mode.value) {
                    ElytraFlightMode.BOOST -> boostMode()
                    ElytraFlightMode.CONTROL -> controlMode(event)
                    ElytraFlightMode.CREATIVE -> creativeMode()
                    ElytraFlightMode.PACKET -> packetMode(event)
                }
            }
        } else if (!outOfDurability) {
            reset(true)
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
        if (!mc.player.onGround && elytraDurability <= 1 && outOfDurability) {
            holdPlayer(event)
        } else if (outOfDurability) outOfDurability = false /* Reset if players is on ground or replace with a new elytra */

        /* wasInLiquid check */
        if (mc.player.inWater || mc.player.isInLava) {
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
            val speedRatio = (MovementUtils.getSpeed() / getSettingSpeed()).toFloat()
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
            checkForLiquid() -> {
                sendChatMessage("$chatName Liquid below, disabling.")
                autoLanding.value = false
            }
            MODULE_MANAGER.getModuleT(LagNotifier::class.java).paused -> {
                holdPlayer(event)
            }
            mc.player.capabilities.isFlying || !mc.player.isElytraFlying || isPacketFlying -> {
                reset(true)
                takeoff(event)
                return
            }
            else -> {
                when {
                    mc.player.posY > getGroundPosY(false) + 1.0 -> {
                        mc.timer.tickLength = 50.0f
                        mc.player.motionY = max(min(-(mc.player.posY - getGroundPosY(false)) / 20.0, -0.5), -5.0)
                    }
                    mc.player.motionY != 0.0 -> { /* Pause falling to reset fall distance */
                        if (!mc.integratedServerIsRunning) mc.timer.tickLength = 200.0f /* Use timer to pause longer */
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
        val closeToGround = mc.player.posY <= getGroundPosY(false) + height && !wasInLiquid && !mc.integratedServerIsRunning
        val lagNotifier = MODULE_MANAGER.getModuleT(LagNotifier::class.java)
        if (!easyTakeOff.value || lagNotifier.paused || mc.player.onGround) {
            if (lagNotifier.paused && mc.player.posY - getGroundPosY(false) > 4.0f) holdPlayer(event) /* Holds player in the air if server is lagging and the distance is enough for taking fall damage */
            reset(mc.player.onGround)
            return
        }
        if (mc.player.motionY < 0 && !highPingOptimize.value || mc.player.motionY < -0.02) {
            if (closeToGround) {
                mc.timer.tickLength = 25.0f
                return
            }
            if (!highPingOptimize.value && !wasInLiquid && !mc.integratedServerIsRunning) { /* Cringe moment when you use elytra flight in single player world */
                event.cancel()
                mc.player.setVelocity(0.0, -0.02, 0.0)
            }
            if (timerControl.value && !mc.integratedServerIsRunning) mc.timer.tickLength = timerSpeed * 2.0f
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
        var strafeYawDeg = 90.0f * mc.player.movementInput.moveStrafe
        strafeYawDeg *= if (mc.player.movementInput.moveForward != 0.0f) mc.player.movementInput.moveForward * 0.5f else 1.0f
        var yawDeg = mc.player.rotationYaw - strafeYawDeg

        yawDeg -= if (mc.player.movementInput.moveForward < 0.0f) 180 else 0
        packetYaw = yawDeg

        return Math.toRadians(yawDeg.toDouble())
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
        val inventoryMove = MODULE_MANAGER.getModuleT(InventoryMove::class.java)
        val moveUp = if (!legacyLookBoost.value) mc.player.movementInput.jump else mc.player.rotationPitch < -10.0f && !isStandingStillH
        val moveDown = if (inventoryMove.isEnabled && !inventoryMove.sneak.value && mc.currentScreen != null || moveUp) false else mc.player.movementInput.sneak

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

    override fun onUpdate() {
        /* Continuously update server side rotation */
        if ((mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE) && isFlying && spoofPitch.value || autoLanding.value) {
            mc.player.rotationYaw += random().toFloat() * 0.005f - 0.0025f
            mc.player.rotationPitch += random().toFloat() * 0.005f - 0.0025f
        }
    }

    override fun onDisable() {
        reset(true)
    }

    override fun onEnable() {
        autoLanding.value = false
        speedPercentage = accelerateStartSpeed.value.toFloat() /* For acceleration */
        hoverTarget = -1.0 /* For control mode */
    }

    private fun defaults() {
        mc.player?.let {
            durabilityWarning.value = true
            threshold.value = 5
            autoLanding.value = false

            easyTakeOff.value = true
            timerControl.value = true
            minTakeoffHeight.value = 0.5f

            accelerateStartSpeed.value = 100
            accelerateTime.value = 0.0f
            autoReset.value = false

            spoofPitch.value = true
            blockInteract.value = false
            forwardPitch.value = 0

            speedBoost.value = 1.0f
            upSpeedBoost.value = 1.0f
            downSpeedBoost.value = 1.0f

            boostPitchControl.value = 20
            ncpStrict.value = true
            legacyLookBoost.value = false
            altitudeHoldControl.value = false
            dynamicDownSpeed.value = false
            speedControl.value = 1.81f
            fallSpeedControl.value = 0.00000000000003f
            downSpeedControl.value = 1.0f
            fastDownSpeedControl.value = 2.0f

            speedCreative.value = 1.8f
            fallSpeedCreative.value = 0.00001f
            upSpeedCreative.value = 1.0f
            downSpeedCreative.value = 1.0f

            speedPacket.value = 1.8f
            fallSpeedPacket.value = 0.00001f
            downSpeedPacket.value = 1.0f

            defaultSetting.value = false
            sendChatMessage("$chatName Set to defaults!")
        }
    }

    init {
        defaultSetting.settingListener = SettingListeners {
            if (defaultSetting.value) defaults()
        }

        /* Reset isFlying states when switching mode */
        mode.settingListener = SettingListeners {
            reset(true)
        }
    }
}
