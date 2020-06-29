package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod.MODULE_MANAGER
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Created by 086 on 11/04/2018.
 * Updated by Itistheend on 28/12/19.
 * Updated by dominikaaaa on 26/05/20
 * Updated by pNoName on 28/05/20
 * Updated by Xiaro on 29/06/20
 *
 * Some of Control mode was written by an anonymous donator who didn't wish to be named.
 */
@Module.Info(
        name = "ElytraFlight",
        description = "Allows infinite and way easier Elytra flying",
        category = Module.Category.MOVEMENT
)
class ElytraFlight : Module() {
    private val mode = register(Settings.e<ElytraFlightMode>("Mode", ElytraFlightMode.CONTROL))
    private val defaultSetting = register(Settings.b("Defaults", false))
    private val durabilityWarning = register(Settings.b("Durability Warning", true))
    private val threshold = register(Settings.integerBuilder("Broken %").withRange(1, 100).withValue(5).withVisibility { durabilityWarning.value }.build())

    /* Takeoff */
    private val easyTakeOff = register(Settings.b("Easy Takeoff", true))
    private val timerControl = register(Settings.booleanBuilder("Takeoff Timer").withValue(true).withVisibility { easyTakeOff.value }.build())

    /* Spoof Pitch */
    private val spoofPitch = register(Settings.booleanBuilder("Spoof Pitch").withValue(true).withVisibility { mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE }.build())
    private val upPitch = register(Settings.integerBuilder("Up Pitch").withRange(-90, 90).withValue(-10).withVisibility { spoofPitch.value && mode.value == ElytraFlightMode.CONTROL }.build())
    private val forwardPitch = register(Settings.integerBuilder("Forward Pitch").withRange(-90, 90).withValue(0).withVisibility { spoofPitch.value && (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE) }.build())

    /* Boost */
    private val speedBoost = register(Settings.floatBuilder("Speed B").withValue(1.0f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())
    private val upSpeedBoost = register(Settings.floatBuilder("Up Speed B").withValue(1.0f).withMinimum(0.0f).withMaximum(2.5f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())
    private val downSpeedBoost = register(Settings.floatBuilder("Down Speed B").withValue(1.0f).withMinimum(0.0f).withMaximum(2.5f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())

    /* Control */
    private val lookBoost = register(Settings.booleanBuilder("Look Boost").withValue(true).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val spaceBarTrigger = register(Settings.booleanBuilder("Space Bar Trigger").withValue(false).withVisibility { lookBoost.value && mode.value == ElytraFlightMode.CONTROL }.build())
    private val autoBoost = register(Settings.booleanBuilder("Auto Boost").withValue(true).withVisibility { lookBoost.value && mode.value == ElytraFlightMode.CONTROL }.build())
    private val hoverControl = register(Settings.booleanBuilder("Hover").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val speedControl = register(Settings.floatBuilder("Speed C").withValue(1.81f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val fallSpeedControl = register(Settings.floatBuilder("Fall Speed C").withValue(0.00000000000003f).withMinimum(0.0f).withMaximum(0.3f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val downSpeedControl = register(Settings.floatBuilder("Down Speed C").withMaximum(10.0f).withMinimum(0.0f).withValue(1.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())

    /* Creative */
    private val speedCreative = register(Settings.floatBuilder("Speed CR").withValue(1.8f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.CREATIVE }.build())
    private val fallSpeedCreative = register(Settings.floatBuilder("Fall Speed CR").withValue(0.0001f).withMinimum(0.0f).withMaximum(0.3f).withVisibility { mode.value == ElytraFlightMode.CREATIVE }.build())

    /* Packet */
    private val speedPacket = register(Settings.floatBuilder("Speed P").withValue(1.3f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.PACKET }.build())
    private val fallSpeedPacket = register(Settings.floatBuilder("Fall Speed P").withValue(0.0001f).withMinimum(0.0f).withMaximum(0.3f).withVisibility { mode.value == ElytraFlightMode.PACKET }.build())

    private enum class ElytraFlightMode {
        BOOST, CONTROL, CREATIVE, PACKET
    }

    private var elytraIsEquipped = false
    private var elytraDurability = 0
    private var outOfDurability = false
    private var isFlying = false

    /* Control mode states */
    private var hoverTarget = -1.0
    private var packetYaw = 0.0f
    private var hoverState = false
    private var isBoosting = false
    private var isStandingStill = false

    /* Event Handlers */
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (!elytraIsEquipped || elytraDurability <= 1 || isBoosting || mc.player == null || !isFlying || mc.player.isSpectator) return@EventHook

        if (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE) packetEventSend(event)
    })

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (!elytraIsEquipped || elytraDurability <= 1 || isBoosting || mc.player == null || !isFlying || mc.player.isSpectator) return@EventHook

        if (mode.value == ElytraFlightMode.CONTROL || mode.value == ElytraFlightMode.CREATIVE) packetEventReceive(event)
    })

    @EventHandler
    private val playerTravelListener = Listener(EventHook { event: PlayerTravelEvent ->
        if (isBoosting || mc.player == null || mc.player.isSpectator) return@EventHook
        stateUpdate(event)

        if (elytraIsEquipped && elytraDurability > 1) {
            if (!isFlying) {
                takeoff(event)
            } else {
                mc.timer.tickLength = 50.0f
                mc.player.isSprinting = false

                when (mode.value) {
                    ElytraFlightMode.BOOST -> boostMode(event)
                    ElytraFlightMode.CONTROL -> controlMode(event)
                    ElytraFlightMode.CREATIVE -> creativeMode(event)
                    ElytraFlightMode.PACKET -> packetMode(event)
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
            if (!mc.player.onGround) {
                if (oldDurability != elytraDurability) {
                    if (durabilityWarning.value && elytraDurability > 1 && elytraDurability < threshold.value * armorSlot.maxDamage / 100) {
                        sendChatMessage("$chatName Warning: Elytra has $elytraDurability durability remaining")
                    } else if (elytraDurability <= 1 && !outOfDurability) {
                        sendChatMessage("$chatName Elytra is out of durability in the air")
                        outOfDurability = true
                    }
                }
            }
        } else elytraDurability = 0

        //* Holds player in the air if run out of durability *//
        if (!mc.player.onGround && outOfDurability && elytraDurability <= 1) {
            event.cancel()
            mc.player.setVelocity(0.0, -0.01, 0.0)
        } else if (outOfDurability) outOfDurability = false //* Reset if players is on ground or replace with a new elytra *//

        isFlying = if (mode.value == ElytraFlightMode.BOOST || mode.value == ElytraFlightMode.CONTROL) mc.player.isElytraFlying else mc.player.isElytraFlying || mc.player.capabilities.isFlying

        /* No movement input check and rotation update */
        if (spoofPitch.value && isFlying) {
            val wasStandingStill = isStandingStill
            isStandingStill = (mc.player.movementInput.moveForward == 0f && mc.player.movementInput.moveStrafe == 0f && !mc.player.movementInput.jump && !mc.player.movementInput.sneak)
            if ((wasStandingStill != isStandingStill) || isStandingStill && (mc.gameSettings.keyBindUseItem.isKeyDown || mc.gameSettings.keyBindAttack.isKeyDown)) mc.player.rotationPitch -= 0.0001f /* update server side pitch when starting moving or clicking */
        } else isStandingStill = false
    }

    /* The best takeoff method <3 */
    private fun takeoff(event: PlayerTravelEvent) {
        // TODO: Pause takeoff if the server is lagging
        if (easyTakeOff.value && !mc.player.onGround && mc.player.motionY < -0.04) {
            if (timerControl.value) mc.timer.tickLength = 200.0f
            mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
            hoverTarget = mc.player.posY + 0.35
        } else if (mc.player.onGround) mc.timer.tickLength = 50.0f /* Reset timer if player is on ground */
    }

    /* Spoof Pitch */
    private fun packetEventSend(event: PacketEvent.Send) {
        if (event.packet is CPacketPlayer.Rotation || event.packet is CPacketPlayer.PositionRotation) {
            val packet = event.packet as CPacketPlayer
            val moveUp = if (!lookBoost.value && mode.value == ElytraFlightMode.CONTROL) mc.player.movementInput.jump else false

            if (isStandingStill && !mc.gameSettings.keyBindUseItem.isKeyDown && !mc.gameSettings.keyBindAttack.isKeyDown) { /* Cancels rotation packets when standing still and not clicking */
                event.cancel()
            }
            if (spoofPitch.value && !isStandingStill) {
                if (moveUp) {
                    packet.pitch = upPitch.value.toFloat()
                } else {
                    packet.pitch = forwardPitch.value.toFloat()
                }
            }
            if (mode.value == ElytraFlightMode.CONTROL) packet.yaw = packetYaw
        }
    }

    /* Cancel server rotation packets to avoid view rubber banding in some cases */
    private fun packetEventReceive(event: PacketEvent.Receive) {
        if (event.packet is SPacketPlayerPosLook) {
            val packet = event.packet as SPacketPlayerPosLook
            packet.pitch = mc.player.rotationPitch
            if (isStandingStill && !mc.gameSettings.keyBindUseItem.isKeyDown && !mc.gameSettings.keyBindAttack.isKeyDown) packet.yaw = mc.player.rotationPitch
        }
    }
    /* End of Generic Functions */

    /* Boost Mode */
    private fun boostMode(event: PlayerTravelEvent) {
        val yaw = Math.toRadians(mc.player.rotationYaw.toDouble())

        mc.player.motionX -= mc.player.movementInput.moveForward * sin(yaw) * speedBoost.value / 20
        if (mc.player.movementInput.jump) mc.player.motionY += upSpeedBoost.value / 15 else if (mc.player.movementInput.sneak) mc.player.motionY -= downSpeedBoost.value / 15
        mc.player.motionZ += mc.player.movementInput.moveForward * cos(yaw) * speedBoost.value / 20
    }

    /* Control Mode */
    private fun controlMode(event: PlayerTravelEvent) {
        // TODO: Need to rewrite this mess
        // No need to look backward to move backward
        // TODO: Old Space upward flight but with variable boosting angle
        val inventoryMove = MODULE_MANAGER.getModuleT(InventoryMove::class.java)
        val moveForward = mc.player.movementInput.moveForward > 0
        val moveBackward = mc.player.movementInput.moveForward < 0
        val moveLeft = mc.player.movementInput.moveStrafe > 0
        val moveRight = mc.player.movementInput.moveStrafe < 0
        val moveUp = if (!lookBoost.value) mc.player.movementInput.jump else false
        val moveDown = if (inventoryMove.isEnabled && !inventoryMove.sneak.value && mc.currentScreen != null) false else mc.player.movementInput.sneak
        val moveForwardFactor = if (moveForward) 1.0f else (if (moveBackward) -1 else 0).toFloat()
        var yawDeg = mc.player.rotationYaw

        if (moveLeft && (moveForward || moveBackward)) {
            yawDeg -= 40.0f * moveForwardFactor
        } else if (moveRight && (moveForward || moveBackward)) {
            yawDeg += 40.0f * moveForwardFactor
        } else if (moveLeft) {
            yawDeg -= 90.0f
        } else if (moveRight) {
            yawDeg += 90.0f
        }

        if (moveBackward) yawDeg -= 180.0f

        packetYaw = yawDeg
        val yaw = Math.toRadians(yawDeg.toDouble())

        if (hoverTarget < 0.0) hoverTarget = mc.player.posY
        hoverState = if (hoverState) mc.player.posY < hoverTarget + 0.1 else mc.player.posY < hoverTarget + 0.0
        val doHover: Boolean = hoverState && hoverControl.value

        val motionAmount = sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)

        if ((moveUp || moveForward || moveBackward || moveLeft || moveRight) && !isStandingStill) { /* Wait for the server side pitch to be updated before moving */
            if ((moveUp || doHover) && motionAmount > 1.0) {
                if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
                    mc.player.motionY = downSpeedControl.value.toDouble()
                } else {
                    val calcMotionDiff = motionAmount * 0.008
                    mc.player.motionY += calcMotionDiff * 3.2
                    mc.player.motionX -= (-sin(yaw)) * calcMotionDiff / 1.0
                    mc.player.motionZ -= cos(yaw) * calcMotionDiff / 1.0

                    mc.player.motionX *= 0.99
                    mc.player.motionY *= 0.98
                    mc.player.motionZ *= 0.99
                }
            } else { /* runs when pressing wasd */
                mc.player.motionX = (-sin(yaw)) * speedControl.value
                mc.player.motionY = (-fallSpeedControl.value).toDouble()
                mc.player.motionZ = cos(yaw) * speedControl.value
            }
        } else { /* Stop moving if no inputs are pressed */
            mc.player.motionX = 0.0
            mc.player.motionY = 0.0
            mc.player.motionZ = 0.0
        }

        if (moveDown) {
            mc.player.motionY = -downSpeedControl.value.toDouble()
        }

        if (moveUp || moveDown) {
            hoverTarget = mc.player.posY
        }
        event.cancel()
    }

    private fun lookBoost() {
        if (mc.player.movementInput.moveForward > 0 && mc.player.movementInput.jump && spaceBarTrigger.value && mc.player.rotationPitch > -10)
            mc.player.rotationPitch = -25f

        val readyToBoost = mc.player.movementInput.moveForward > 0 && ((mc.player.movementInput.jump && spaceBarTrigger.value) || !spaceBarTrigger.value) && mc.player.rotationPitch <= -10
        val shouldAutoBoost = !autoBoost.value || ((mc.player.motionY >= (-fallSpeedControl.value) && sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ) >= 0.8) || (mc.player.motionY >= 1))

        if ((readyToBoost && shouldAutoBoost) != isBoosting)
            mc.player.rotationPitch -= 0.0001f /* Tried with sending rotation packet, doesn't work on 2b2t.org */

        isBoosting = readyToBoost && shouldAutoBoost
    }
    /* End of Control Mode */

    /* Creative Mode */
    private fun creativeMode(event: PlayerTravelEvent) {
        if (mc.player.onGround) {
            mc.player.capabilities.isFlying = false
            return
        }
        mc.player.capabilities.isFlying = true
        mc.player.jumpMovementFactor = speedCreative.value
        mc.player.capabilities.flySpeed = speedCreative.value
        val motionY = if (isStandingStill) mc.player.motionY else mc.player.motionY - fallSpeedCreative.value.toDouble()
        mc.player.setVelocity(0.0, motionY, 0.0)
    }

    /* Packet Mode */
    private fun packetMode(event: PlayerTravelEvent) {
        if (mc.player.isElytraFlying) mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
        mc.player.capabilities.isFlying = true
        mc.player.jumpMovementFactor = speedPacket.value
        mc.player.capabilities.flySpeed = speedPacket.value / 8
        mc.player.motionY = if (isStandingStill) mc.player.motionY else mc.player.motionY - fallSpeedPacket.value.toDouble()
    }

    override fun onUpdate() {
        if (mc.player == null || mc.player.isSpectator) return

        if (mode.value == ElytraFlightMode.CONTROL) {
            if (lookBoost.value) {
                lookBoost()
            } else {
                isBoosting = false
            }
            return
        }
    }

    override fun onDisable() {
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
            upPitch.value = -10
            forwardPitch.value = 0

            speedBoost.value = 1.0f
            upSpeedBoost.value = 1.0f
            downSpeedBoost.value = 1.0f

            lookBoost.value = true
            spaceBarTrigger.value = false
            autoBoost.value = true
            hoverControl.value = false

            speedControl.value = 1.81f
            fallSpeedControl.value = 0.00000000000003f
            downSpeedControl.value = 1.0f

            speedPacket.value = 1.8f
            fallSpeedPacket.value = 0.00001f

            speedPacket.value = 1.3f
            fallSpeedPacket.value = 0.00001f

            defaultSetting.value = false
            sendChatMessage("$chatName Set to defaults!")
            closeSettings()
        }
    }

    init {
        defaultSetting.settingListener = SettingListeners { if (defaultSetting.value) defaults() }
    }
}