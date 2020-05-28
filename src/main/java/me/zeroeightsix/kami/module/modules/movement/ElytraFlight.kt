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
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.MathHelper
import kotlin.math.sqrt

/**
 * Created by 086 on 11/04/2018.
 * Updated by Itistheend on 28/12/19.
 * Updated by dominikaaaa on 26/05/20
 * Some of Control mode was written by an anonymous donator who didn't wish to be named.
 * Updated by pNoName on 28/05/20
 */
@Module.Info(
        name = "ElytraFlight",
        description = "Allows infinite and way easier Elytra flying",
        category = Module.Category.MOVEMENT
)
class ElytraFlight : Module() {
    private val mode = register(Settings.e<ElytraFlightMode>("Mode", ElytraFlightMode.HIGHWAY))
    private val defaultSetting = register(Settings.b("Defaults", false))

    /* Highway */
    private val easyTakeOff = register(Settings.booleanBuilder("Easy Takeoff H").withValue(true).withVisibility { mode.value == ElytraFlightMode.HIGHWAY }.build())
    private val takeOffMode = register(Settings.enumBuilder(TakeoffMode::class.java).withName("Takeoff Mode").withValue(TakeoffMode.PACKET).withVisibility { easyTakeOff.value && mode.value == ElytraFlightMode.HIGHWAY }.build())
    private val speedHighway = register(Settings.floatBuilder("Speed H").withValue(1.8f).withMaximum(1.8f).withVisibility { mode.value == ElytraFlightMode.HIGHWAY }.build())
    private val fallSpeedHighway = register(Settings.floatBuilder("Fall Speed H").withValue(0.000100000002f).withVisibility { mode.value == ElytraFlightMode.HIGHWAY }.build())

    /* Fly or Boost */
    private val fallSpeed = register(Settings.floatBuilder("Fall Speed").withValue(-.003f).withVisibility { mode.value != ElytraFlightMode.CONTROL && mode.value != ElytraFlightMode.HIGHWAY }.build())
    private val upSpeedBoost = register(Settings.floatBuilder("Up Speed B").withValue(0.08f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())
    private val downSpeedBoost = register(Settings.floatBuilder("Down Speed B").withValue(0.04f).withVisibility { mode.value == ElytraFlightMode.BOOST }.build())

    /* Control */
    private val upPitch = register(Settings.integerBuilder("Up Pitch").withRange(-90, 90).withValue(-10).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val forwardPitch = register(Settings.integerBuilder("Forward Pitch").withRange(-90, 90).withValue(0).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val lookBoost = register(Settings.booleanBuilder("Look Boost").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val hoverControl = register(Settings.booleanBuilder("Hover").withValue(false).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val easyTakeOffControl = register(Settings.booleanBuilder("Easy Takeoff C").withValue(true).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val timerControl = register(Settings.booleanBuilder("Takeoff Timer").withValue(true).withVisibility { easyTakeOffControl.value && mode.value == ElytraFlightMode.CONTROL }.build())
    private val speedControl = register(Settings.floatBuilder("Speed C").withValue(1.8f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val fallSpeedControl = register(Settings.floatBuilder("Fall Speed C").withValue(0.000100000002f).withMaximum(0.3f).withMinimum(0.0f).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())
    private val downSpeedControl = register(Settings.doubleBuilder("Down Speed C").withMaximum(10.0).withMinimum(0.0).withValue(2.0).withVisibility { mode.value == ElytraFlightMode.CONTROL }.build())

    /* Control mode states */
    private var hoverTarget = -1.0
    private var packetYaw = 0.0f
    private var hoverState = false
    private var isBoosting = false

    /* Control Mode */
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (isBoosting || mode.value != ElytraFlightMode.CONTROL || mc.player == null || mc.player.isSpectator) return@EventHook

        if (event.packet is CPacketPlayer) {
            if (!mc.player.isElytraFlying) return@EventHook
            val packet = event.packet as CPacketPlayer
            val moveUp = if (!lookBoost.value) mc.player.movementInput.jump else false
            if (moveUp) {
                packet.pitch = upPitch.value.toFloat()
            } else {
                packet.pitch = forwardPitch.value.toFloat()
            }
            packet.yaw = packetYaw
        }

        if (event.packet is CPacketEntityAction && (event.packet as CPacketEntityAction).action == CPacketEntityAction.Action.START_FALL_FLYING) {
            hoverTarget = mc.player.posY + 0.35
        }
    })

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (isBoosting || mode.value != ElytraFlightMode.CONTROL || mc.player == null || !mc.player.isElytraFlying || mc.player.isSpectator) return@EventHook
        if (event.packet is SPacketPlayerPosLook) {
            val packet = event.packet as SPacketPlayerPosLook
            packet.pitch = mc.player.rotationPitch
        }
    })

    @EventHandler
    private val playerTravelListener = Listener(EventHook { event: PlayerTravelEvent ->
        if (isBoosting || mode.value != ElytraFlightMode.CONTROL || mc.player == null || mc.player.isSpectator) return@EventHook

        if (!mc.player.isElytraFlying) {
            if (easyTakeOffControl.value && !mc.player.onGround && mc.player.motionY < -0.04) {
                mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))

                if (timerControl.value) mc.timer.tickLength = 200.0f
                event.cancel()
                return@EventHook
            }
            return@EventHook
        }
        mc.timer.tickLength = 50.0f

        if (hoverTarget < 0.0) hoverTarget = mc.player.posY

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
        val yaw = Math.toRadians(yawDeg.toDouble()).toFloat()
        val motionAmount = sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)
        hoverState = if (hoverState) mc.player.posY < hoverTarget + 0.1 else mc.player.posY < hoverTarget + 0.0
        val doHover: Boolean = hoverState && hoverControl.value

        if (moveUp || moveForward || moveBackward || moveLeft || moveRight) {
            if ((moveUp || doHover) && motionAmount > 1.0) {
                if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
                    mc.player.motionY = downSpeedControl.value
                } else {
                    val calcMotionDiff = motionAmount * 0.008
                    mc.player.motionY += calcMotionDiff * 3.2
                    mc.player.motionX -= (-MathHelper.sin(yaw)).toDouble() * calcMotionDiff / 1.0
                    mc.player.motionZ -= MathHelper.cos(yaw).toDouble() * calcMotionDiff / 1.0

                    mc.player.motionX *= 0.99
                    mc.player.motionY *= 0.98
                    mc.player.motionZ *= 0.99
                }
            } else { /* runs when pressing wasd */
                mc.player.motionX = (-MathHelper.sin(yaw)).toDouble() * speedControl.value
                mc.player.motionY = (-fallSpeedControl.value).toDouble()
                mc.player.motionZ = MathHelper.cos(yaw).toDouble() * speedControl.value
            }
        } else { /* Stop moving if no inputs are pressed */
            mc.player.motionX = 0.0
            mc.player.motionY = 0.0
            mc.player.motionZ = 0.0
        }

        if (moveDown) {
            mc.player.motionY = -downSpeedControl.value
        }

        if (moveUp || moveDown) {
            hoverTarget = mc.player.posY
        }
        event.cancel()
    })
    /* End of Control Mode */

    override fun onUpdate() {
        if (mc.player == null || mc.player.isSpectator) return

        if (mode.value == ElytraFlightMode.CONTROL) {
            isBoosting = (mc.player.rotationPitch < -10  && !(mc.player.motionX.toInt() == 0 && mc.player.motionY.toInt() == 0 && mc.player.motionZ.toInt() == 0)) && lookBoost.value
            return
        }

        takeoffHighway()
        setFlySpeed()

        /* required on some servers in order to land */
        if (mc.player.onGround) mc.player.capabilities.allowFlying = false

        if (mc.player.isElytraFlying) modeNonControl()
    }

    private fun modeNonControl() {
        if (mode.value == ElytraFlightMode.BOOST) {
            if (mc.player.isInWater) {
                mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
                return
            }

            if (mc.gameSettings.keyBindJump.isKeyDown) mc.player.motionY += upSpeedBoost.value else if (mc.gameSettings.keyBindSneak.isKeyDown) mc.player.motionY -= downSpeedBoost.value

            if (mc.gameSettings.keyBindForward.isKeyDown) {
                val yaw = Math.toRadians(mc.player.rotationYaw.toDouble()).toFloat()
                mc.player.motionX -= MathHelper.sin(yaw) * 0.05f.toDouble()
                mc.player.motionZ += MathHelper.cos(yaw) * 0.05f.toDouble()
            } else if (mc.gameSettings.keyBindBack.isKeyDown) {
                val yaw = Math.toRadians(mc.player.rotationYaw.toDouble()).toFloat()
                mc.player.motionX += MathHelper.sin(yaw) * 0.05f.toDouble()
                mc.player.motionZ -= MathHelper.cos(yaw) * 0.05f.toDouble()
            }
        } else if (mode.value == ElytraFlightMode.HIGHWAY || mode.value == ElytraFlightMode.FLY) {
            mc.player.capabilities.flySpeed = .915f
            mc.player.capabilities.isFlying = true

            if (mc.player.capabilities.isCreativeMode) return
            mc.player.capabilities.allowFlying = true
        }
    }

    private fun setFlySpeed() {
        if (mc.player.capabilities.isFlying) {
            if (mode.value == ElytraFlightMode.HIGHWAY) {
                mc.player.isSprinting = false
                mc.player.setVelocity(0.0, 0.0, 0.0)
                mc.player.setPosition(mc.player.posX, mc.player.posY - fallSpeedHighway.value, mc.player.posZ)
                mc.player.capabilities.flySpeed = speedHighway.value
            } else if (mode.value == ElytraFlightMode.BOOST || mode.value == ElytraFlightMode.FLY) {
                mc.player.setVelocity(0.0, 0.0, 0.0)
                mc.player.capabilities.flySpeed = .915f
                mc.player.setPosition(mc.player.posX, mc.player.posY - fallSpeed.value, mc.player.posZ)
            }
        }
    }

    private fun takeoffHighway() {
        if (!(mode.value == ElytraFlightMode.HIGHWAY && easyTakeOff.value)) return
        if (!mc.player.isElytraFlying && !mc.player.onGround) {
            when (takeOffMode.value) {
                TakeoffMode.CLIENT -> {
                    mc.player.capabilities.isFlying = true
                    mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
                }
                TakeoffMode.PACKET -> mc.connection!!.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING))
            }
        }
        if (mc.player.isElytraFlying) {
            easyTakeOff.value = false
            sendChatMessage("$chatName Disabled takeoff!")
        }
    }

    override fun onDisable() {
        mc.timer.tickLength = 50.0f
        mc.player.capabilities.flySpeed = 0.05f

        if (mc.player.capabilities.isCreativeMode) return
        mc.player.capabilities.isFlying = false
        mc.player.capabilities.allowFlying = false
    }

    override fun onEnable() {
        hoverTarget = -1.0 /* For control mode */
    }

    private fun defaults() {
        mc.player?.let {
            easyTakeOff.value = true
            hoverControl.value = true
            easyTakeOffControl.value = false
            timerControl.value = false
            takeOffMode.value = TakeoffMode.PACKET
            speedHighway.value = 1.8f
            speedControl.value = 1.8f
            fallSpeedHighway.value = 0.000100000002f
            fallSpeedControl.value = 0.000100000002f
            fallSpeed.value = -.003f
            upSpeedBoost.value = 0.08f
            downSpeedBoost.value = 0.04f
            downSpeedControl.value = 2.0
            defaultSetting.value = false
            sendChatMessage("$chatName Set to defaults!")
            closeSettings()
        }
    }

    private enum class ElytraFlightMode {
        BOOST, FLY, CONTROL, HIGHWAY
    }

    private enum class TakeoffMode {
        CLIENT, PACKET
    }

    init {
        defaultSetting.settingListener = SettingListeners { if (defaultSetting.value) defaults() }
    }
}
