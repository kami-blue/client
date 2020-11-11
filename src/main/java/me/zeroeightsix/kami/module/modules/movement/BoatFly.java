package com.gitlab.mrmrmr.module.movement;


import com.gitlab.mrmrmr.events.EventInputPacketEvent;
import com.gitlab.mrmrmr.events.EventOutputPacketEvent;
import com.gitlab.mrmrmr.events.EventPlayerUpdate;
import com.gitlab.mrmrmr.utils.MovementUtils;
import com.gitlab.mrmrmr.events.client.EventClientTick;
import me.ionar.salhack.events.MinecraftEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

//https://github.com/kami-blue/client/blob/e67b682f4b14232a24b3a8083232c3cd8798e4f9/src/main/java/me/zeroeightsix/kami/module/modules/movement/EntitySpeed.kt
public class BoatFly extends Module
{
    public final Value<Double> upSpeed = new Value("Up Speed", new String[]
            {  }, "+motionY of the obat", 1.0, 0, 10, 0.1);

    public final Value<Double> downSpeed = new Value("Down Speed", new String[]
            {  }, "-motionY of the boat in the air", 1.0, 0, 10, 0.1);

    public final Value<Double> horizSpeed = new Value("Fly Speed", new String[]
            {  }, "Sets the fly speed of the boat", 1.0, 0, 10, 0.1);

    public final Value<Double> flyDownSpeed = new Value("Fly Down Speed", new String[]
            {  }, "-motionY of the boat when flying. Requires safety leave to be enabled.", 1.0, 0, 10, 0.1);

    public final Value<Boolean> leaveOnlyOnGround = new Value("Safety Leave", new String[]
            {  }, "Only leave the boat when on the ground. Required to fly down", true);

    public final Value<Boolean> spamInteract = new Value("Spam Interact", new String[]
            {  }, "Spams UseEntity on every VehicleMove", false);

    public final Value<Boolean> ignoreVehicleMove = new Value("Block SVehicleMove", new String[]
            {  }, "Ignore clientbound vehicle movement packets", true);

    public final Value<Boolean> ignorePlayerPosRot = new Value("Fake SPlayerPosRot", new String[]
            {  }, "Fake clientbound player position/rotation packets", true);

    public final Value<Integer> interactTickDelay = new Value("Interact Delay", new String[]
            {  }, "How many ticks to delay between each UseEntity", 1, 0, 19, 1);

    public final Value<Integer> distance = new Value("Distance", new String[]
            {  }, "Distance to load new chunks", 1, 1, 50, 1);

    public final Value<Double> boatOpacity = new Value("Boat Opacity", new String[]
            {  }, "Boat opacity", 1.0, 0.0, 1.0, 0.01);

    public final Value<Boolean> vanillaFlyBypass = new Value("Vehicle Fly Bypass", new String[]
            {  }, "Bypass vanilla vehicle movement checks", true);

    public final Value<Integer> ticksTillDescend = new Value("Ticks Till Descend", new String[]
            {  }, "How many ticks in between each descend", 1, 0, 80, 1);

    int vanillaFlyCounter;

    private boolean loadChunks = false;

    private boolean sending = false;

    private double x, y, z;

    public BoatFly()
    {
        super("BoatFly", new String[]
                {  }, "Boatfly with NCP bypass", "NONE", -1, ModuleType.MOVEMENT);
    }

    private ArrayList<Packet> tmp = new ArrayList<>();

    @EventHandler
    private Listener<EventPlayerUpdate> TravelEvent = new Listener<>(p_Event ->
    {
        if(p_Event.getEra() == MinecraftEvent.Era.POST)
            return;

        Minecraft mc = Minecraft.getMinecraft();

        // check if riding
        if(!mc.player.isRiding() || ! (mc.player.getRidingEntity() instanceof EntityBoat) || !mc.player.connection.doneLoadingTerrain)
            return;

        EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();

        steerEntity(boat);
        fly(boat);

        if(vanillaFlyBypass.getValue())
            vanillaFlyCounter++;
    });

    private void steerEntity(Entity entity) {
        double yawRad = MovementUtils.INSTANCE.calcMoveYaw();

        double motionX = -Math.sin(yawRad) * horizSpeed.getValue().doubleValue();
        double motionZ = Math.cos(yawRad) * horizSpeed.getValue().doubleValue();


        // && !isBorderingChunk(entity, motionX, motionZ)
        if (MovementUtils.INSTANCE.isInputing()) {
            entity.motionX = motionX;
            entity.motionZ = motionZ;
        } else {
            entity.motionX = 0.0;
            entity.motionZ = 0.0;
        }

        //checkLoadChunks(entity, entity.motionX, entity.motionZ);

        if (entity instanceof EntityHorse || entity instanceof EntityBoat) {
            entity.rotationYaw = mc.player.rotationYaw;

            // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
            if (entity instanceof EntityBoat)
                ((EntityBoat) entity).updateInputs(false, false, false, false);
        }

        double d = Math.sqrt(Math.pow(this.x - entity.posX, 2) + Math.pow(this.z - entity.posZ, 2));

        if (d >= distance.getValue()) {
            this.x = entity.posX;
            this.z = entity.posZ;
            loadChunks = true;
        }
        else {
            loadChunks = false;
        }
    }

    private void fly(Entity entity) {
        if (!entity.isInWater())
            entity.motionY = -downSpeed.getValue().doubleValue();

        if (mc.gameSettings.keyBindJump.isKeyDown())
            entity.motionY = upSpeed.getValue().doubleValue();

        if(flyingDown)
            entity.motionY -= flyDownSpeed.getValue();
    }

    @EventHandler
    private Listener<EventOutputPacketEvent> PacketOutEvent = new Listener<>(p_Event ->
    {
        if(sending)
            return;

        if(mc.player != null && (!mc.player.isRiding() || ! (mc.player.getRidingEntity() instanceof EntityBoat)))
            return;

        spamVehicleMove(p_Event);
    });

    @EventHandler
    private Listener<EventInputPacketEvent> PacketInEvent = new Listener<>(p_Event ->
    {
        if (mc.player == null)
            return;

        if(!mc.player.isRiding() || ! (mc.player.getRidingEntity() instanceof EntityBoat))
            return;

        if (((p_Event.getPacket() instanceof SPacketMoveVehicle || p_Event.getPacket() instanceof SPacketEntityVelocity) && this.mc.player.getRidingEntity() instanceof EntityBoat) && ignoreVehicleMove.getValue())
            p_Event.cancel();

/*        if (p_Event.getPacket() instanceof SPacketPlayerPosLook && ignorePlayerPosRot.getValue() && !loadChunks && mc.player.connection.doneLoadingTerrain){
            p_Event.cancel();
        }*/

        //Try to smooth out the jitteriness as much as possible
        if(p_Event.getPacket() instanceof SPacketPlayerPosLook){
            respondToPosLook(p_Event);
        }
    });

    boolean flyingDown = false;

    public void spamVehicleMove(EventOutputPacketEvent p_Event){
        Packet p = p_Event.getPacket();
        if (p instanceof CPacketVehicleMove) {
            CPacketVehicleMove pp = (CPacketVehicleMove) p;
            if (spamInteract.getValue() && mc.player.getRidingEntity() instanceof EntityBoat && mc.player.ticksExisted % (interactTickDelay.getValue() + 1) == 0) {
                this.mc.getConnection().sendPacket(new CPacketUseEntity(this.mc.player.getRidingEntity(), EnumHand.MAIN_HAND));
            }


            if(vanillaFlyCounter >= ticksTillDescend.getValue()){
                sending = true;
                pp.y -= 0.5;
                this.mc.getConnection().sendPacket(pp);
                pp.y += 0.2;
                vanillaFlyCounter = 0;
                SalHack.SendMessage("Went down " + pp.y);
                sending = false;
            }
        }

        if(p instanceof CPacketInput && leaveOnlyOnGround.getValue()){
            CPacketInput input = (CPacketInput) p;
            if (!(this.mc.player.getRidingEntity().onGround || this.mc.player.getRidingEntity().isInWater()) && input.isSneaking()) {
                p_Event.cancel();
                flyingDown = true;
            }
            else {
                flyingDown = false;
            }
        }
    }

    private double calculateGround(double posX, double posY, double posZ) {
        double ground = posY;

        while (ground > 0.0) {
            if (!mc.world.isAirBlock(new BlockPos(posX, ground, posZ))) {
                return ground;
            }
            ground -= 1;
        }

        return 0.0;
    }

    public double getOpacity(){
        return boatOpacity.getValue();
    }

    public void respondToPosLook(EventInputPacketEvent p_Event){
        if (mc.world == null || mc.player == null)
            return;

        if (p_Event.getPacket() instanceof SPacketPlayerPosLook)
        {
            if (mc.player != null && mc.getConnection().doneLoadingTerrain)
            {
                p_Event.cancel();
                EntityPlayer entityplayer = mc.player;
                final SPacketPlayerPosLook packetIn = (SPacketPlayerPosLook) p_Event.getPacket();
                double d0 = packetIn.getX();
                double d1 = packetIn.getY();
                double d2 = packetIn.getZ();

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X))
                {
                    d0 += entityplayer.posX;
                }

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y))
                {
                    d1 += entityplayer.posY;
                }

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z))
                {
                    d2 += entityplayer.posZ;
                }

                mc.getConnection().sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
                mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(d0, entityplayer.getEntityBoundingBox().minY, d2, packetIn.yaw, packetIn.pitch, false));
            }
        }
    }
}
