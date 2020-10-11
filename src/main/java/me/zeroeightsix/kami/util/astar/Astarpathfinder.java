package me.zeroeightsix.kami.util.astar;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.PlayerTravelEvent;
import me.zeroeightsix.kami.module.modules.movement.AutoWalk;
import me.zeroeightsix.kami.module.modules.movement.ElytraFlight;
import me.zeroeightsix.kami.util.BlockUtils;

import me.zeroeightsix.kami.util.combat.SurroundUtils;
import me.zeroeightsix.kami.util.math.MathUtils;
import me.zeroeightsix.kami.util.math.RotationUtils;
import me.zeroeightsix.kami.util.text.MessageSendHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by fred41 on 18/07/2020.
 * <p>
 * I went full static here i dont know why but its easier at some points and im to lazy to change it.
 * It doesnt matter tho as we never have more than one instance
 */
public class Astarpathfinder {

    private static ElytraFlight elytraFlight = ElytraFlight.INSTANCE;
    public static int nopathfound;
    private static BlockPos currentplayerBlockpos;
    private static int currentDirection;

    private static Vec3i startplayerpos = new Vec3i(0, 0, 0);
    public static double currentyaw;
    public static List<AstarNode> path = new ArrayList<>();
    private static Minecraft mc = Minecraft.getMinecraft();
    private static long lastStuckCheck = 0;
    private static Vec3d currentStuckCheckPos = new Vec3d(0, 0, 0);
    private static Vec3d lastStuckCheckPos = new Vec3d(0, 0, 0);
    private static boolean baritonePathStarted;
    private static TrackState buildstate = TrackState.GOTO_BUILD;
    private static State state = State.START_FLYING;
    private static BlockPos takeoffPos;
    private static int rubberbandcounter = 0;
    private static double laspacketx;
    private static double laspacketz;
    private static int roof;
    private static PlayerTravelEvent currentEvent;

    private static boolean flightcheckdelaystarted;
    private static long flightcheckdelay;
    public static boolean holdinair;
    private static int direction;
    private static BlockPos stuckPos;
    private static boolean landed;
    public static boolean enabled;
    private static boolean blockstuck;
    private static double oldhovertarget;

    enum State {
        START_FLYING,
        GOTO_TRACK,
        LAND_SAVE,
        PATHFIND
    }

    enum TrackState {
        GOTO_BUILD,
        MINE_RADIUS,
        GOTO_TAKEOFF,
        CENTER_ON_BLOCK
    }


    public static void init() {

        if (mc.player != null) {
            enabled = true;
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            startplayerpos = getPlayerBlockpos();

            path = null;
            direction = currentDirection = (MathHelper.floor(mc.player.rotationYaw * 8.0f / 360.0f + 0.5) & 0x7);
            MessageSendHelper.sendChatMessage("Pathfinding in " + MathUtils.Cardinal.values()[currentDirection].cardinalName + " direction");

            if (mc.player.isElytraFlying()) {
                elytraFlight.setHoverTarget(Math.floor(mc.player.posY) + 0.3);
                state = State.PATHFIND;
                startplayerpos = new BlockPos(startplayerpos.x, startplayerpos.y - 1, startplayerpos.z);
            } else
                state = State.GOTO_TRACK;
            takeoffPos = getPlayerBlockpos();
            buildstate = TrackState.CENTER_ON_BLOCK;
            nopathfound = 0;
            lastStuckCheck = 0;
            currentStuckCheckPos = new Vec3d(0, 0, 0);
            baritonePathStarted = false;
            rubberbandcounter = 0;
            laspacketz = -1;
            laspacketx = -1;
            flightcheckdelaystarted = false;
            roof = mc.player.dimension == -1 ? 121 : 255;
            oldhovertarget = elytraFlight.getHoverTarget();
        }

    }

    public static void turnOff() {
        if (mc.player != null) {
            MessageSendHelper.sendChatMessage("Pathfinding disabled");
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }
        enabled = false;
        path = null;
    }


    public static void onreceive(PacketEvent.Receive event) {
        if (!enabled) return;
        if (event.getPacket() instanceof SPacketEntityMetadata) {
            SPacketEntityMetadata meta = (SPacketEntityMetadata) event.getPacket();
            if (meta.getEntityId() == mc.player.getEntityId()) {
                for (EntityDataManager.DataEntry d : meta.getDataManagerEntries()) {
                    if (d.getKey().getId() == 11 && ((float) d.getValue()) < mc.player.getAbsorptionAmount())
                        checkstuckInBlock();

                }
            }
        } else if (event.getPacket() instanceof SPacketUpdateHealth) {
            SPacketUpdateHealth packet = (SPacketUpdateHealth) event.getPacket();
            if (packet.getHealth() < mc.player.getHealth()) {
                checkstuckInBlock();
            }
        } else if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
            if ((Math.abs(packet.x - laspacketx) < 0.05 && Math.abs(packet.z - laspacketz) < 0.05)) {
                rubberbandcounter++;
            } else {
                rubberbandcounter = 0;
            }
            laspacketx = packet.x;
            laspacketz = packet.z;
        }
    }

    private static void checkstuckInBlock() {
        if (enabled && (state == State.PATHFIND || (state == State.LAND_SAVE && !landed)) &&
                !mc.player.onGround &&
                !mc.player.isElytraFlying()) {
            blockstuck = true;
            mc.timer.tickLength = 50.0f;
            mc.player.motionZ = 0;
            mc.player.motionX = 0;
            mc.player.motionY = -0.4;
            mc.player.capabilities.isFlying = false;
            mc.player.capabilities.setFlySpeed(0.05f);
        }
    }

    public static void update(PlayerTravelEvent event) {
        currentEvent = event;
        switch (state) {
            case START_FLYING:
                startFlying();
                break;
            case PATHFIND:
                pathfind();
                break;
            case GOTO_TRACK:
                gotoTrack();
                break;
            case LAND_SAVE:
                landSave();
                break;
        }

    }


    private static void landSave() {
        if (mc.player.onGround || landed) {
            landed = true;
            mc.timer.tickLength = 50.0f;
            if (Math.abs(getPlayerBlockpos().getX() - stuckPos.getX()) < 3 && Math.abs(getPlayerBlockpos().getZ() - stuckPos.getZ()) < 3) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                buildstate = TrackState.GOTO_BUILD;
                state = State.GOTO_TRACK;
                currentDirection = direction;
                landed = false;
                blockstuck = false;

            } else {
                if (BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal() == null) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(stuckPos.getX(), stuckPos.getZ()));
                }
            }
        } else {
            if (!BlockUtils.INSTANCE.checkForLiquid()) {
                // if blockstuck let vanilla mechanics run (i.e dont cancel the event) seems to work fine
                if (!blockstuck)
                    elytraFlight.landing(currentEvent);
            } else {
                currentDirection = direction + 4 % 8;
                pathfind();
            }
        }
    }


    private static void pathfind() {
        mc.timer.tickLength = 50;
        mc.player.setSprinting(false);
        holdinair = false;
        currentplayerBlockpos = getPlayerBlockpos();
        calcPath(true);
        removeAllBeforeClosest();
        if (!checkNextChunkLoaded()) {
            holdinair = true;
        } else {
            if (checkStuck() || mc.player.onGround || (!mc.player.isElytraFlying())) {
                lastStuckCheckPos = new Vec3d(0, 0, 0);
                currentStuckCheckPos = new Vec3d(0, 0, 0);
                path = null;
                stuckPos = getPlayerBlockpos();
                state = State.LAND_SAVE;
                nopathfound = 0;
                elytraFlight.flyByMode(currentEvent);
                return;
            }
        }
        float yawDeg = 0;
        if (path != null && path.size() > 1) {
            BlockPos lookat = path.get(Math.min(path.size() - 1, 1)).pos;
            yawDeg = (float) RotationUtils.INSTANCE.getRotationTo(new Vec3d(lookat.getX() + 0.5, mc.player.posY, lookat.getZ() + 0.5), false,1F).getX();
        }
        currentyaw = yawDeg;
        elytraFlight.flyByMode(currentEvent);


    }

    private static boolean checkNextChunkLoaded() {
        return mc.world.getChunk(getPlayerBlockpos()).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(0, 0, 16)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(0, 0, -16)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(16, 0, 0)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(-16, 0, 0)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(16, 0, 16)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(-16, 0, 16)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(16, 0, -16)).isLoaded() &&
                mc.world.getChunk(getPlayerBlockpos().add(-16, 0, -16)).isLoaded();

    }

    public static void startlanding() {
        lastStuckCheckPos = new Vec3d(0, 0, 0);
        currentStuckCheckPos = new Vec3d(0, 0, 0);
        path = null;
        stuckPos = getPlayerBlockpos();
        state = State.LAND_SAVE;
        nopathfound = 0;
    }

    private static boolean checkStuck() {
        if (System.currentTimeMillis() - lastStuckCheck > 1500) {
            lastStuckCheck = System.currentTimeMillis();
            lastStuckCheckPos = currentStuckCheckPos;
            currentStuckCheckPos = mc.player.getPositionVector();
            if (lastStuckCheckPos.distanceTo(currentStuckCheckPos) < 1.3) {
                MessageSendHelper.sendChatMessage("Stuck! moved too little.");
                return true;
            }
        }
        if (nopathfound > 10) {
            MessageSendHelper.sendChatMessage("Stuck! no path found.");
            return true;
        }
        if (path != null && !path.isEmpty() && calcPathLength() / Math.sqrt(Math.pow(mc.player.posX - path.get(path.size() - 1).pos.getX(), 2) + Math.pow(mc.player.posZ - path.get(path.size() - 1).pos.getZ(), 2)) > 2) {
            //this is important for it sometimes tries to find a way through the tunnel you came from and the adjacent unloaded chunks.
            MessageSendHelper.sendChatMessage("Stuck! path too long.");
            return true;
        }
        if (rubberbandcounter > 3) {
            rubberbandcounter = 0;
            MessageSendHelper.sendChatMessage("Stuck! rubberbanding too much.");
            return true;
        }
        return false;

    }

    private static void startFlying() {
        if (mc.player.onGround) {
            mc.player.jump();
            return;
        }
        if (!mc.player.isElytraFlying()) {
            flightcheckdelaystarted = false;
            elytraFlight.takeoff(currentEvent);
        } else {
            if (!flightcheckdelaystarted) {
                flightcheckdelay = System.currentTimeMillis();
            }
            flightcheckdelaystarted = true;
            if (System.currentTimeMillis() - flightcheckdelay > AutoWalk.INSTANCE.getFlydelay().getValue()) {
                if (mc.player.isElytraFlying()) {
                    state = State.PATHFIND;
                    flightcheckdelaystarted = false;
                }
            }
        }
    }


    private static void removeAllBeforeClosest() {
        if (path == null) return;
        double dist = Double.POSITIVE_INFINITY;
        int closest = 0;
        double tempdist;
        for (int i = 0; i < path.size(); i++) {
            if ((tempdist = Math.sqrt(Math.pow(path.get(i).pos.getX() + 0.5 - mc.player.posX, 2) + Math.pow(path.get(i).pos.getZ() + 0.5 - mc.player.posZ, 2))) < dist) {
                dist = tempdist;
                closest = i;
            }
        }
        if (dist < elytraFlight.getSpeedControl().getValue() / 2.0f) {//Half flyspeed for now
            path.subList(0, closest + 1).clear();
        }
    }

    private static void calcPath(boolean forcenew) {

        if (calcPathLength() > 20 && elytraFlight.getHoverTarget() == oldhovertarget)
            return;// dont calculate a new one itf the old one s still long enough.
        List<AstarNode> newpath = new ArrayList<>();
        oldhovertarget = elytraFlight.getHoverTarget();
        //atm a new path gets forced evreytime the current one is shorter than 20 blocks.
        //should change this in the future tho.
        boolean continuePath = (!forcenew && path != null && !path.isEmpty());
        BlockPos startpath = continuePath ? path.get(path.size() - 1).pos : currentplayerBlockpos.add(0, -currentplayerBlockpos.y + elytraFlight.getHoverTarget(), 0);
        int addlayer = 0;
        switch (currentDirection) {
            case 0:
                //+z
                newpath = AstarAlg.findPath(startpath,
                        new BlockPos(startplayerpos.x, elytraFlight.getHoverTarget(), currentplayerBlockpos.getZ() + 40),
                        addlayer);
                break;
            case 1:
                //-x+z
                newpath = AstarAlg.findPath(startpath,//+- / -+ diagonals are strange this somehow fixes it
                        new BlockPos(currentplayerBlockpos.getX() - 40, elytraFlight.getHoverTarget(), startplayerpos.z + Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() - 40))),
                        addlayer);
                break;
            case 2:
                //-x
                newpath = AstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() - 40, elytraFlight.getHoverTarget(), startplayerpos.z),
                        addlayer);
                break;
            case 3:
                //-x-z
                newpath = AstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() - 40, elytraFlight.getHoverTarget(), startplayerpos.z - Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() - 40))),
                        addlayer);
                break;
            case 4:
                //-z
                newpath = AstarAlg.findPath(startpath,
                        new BlockPos(startplayerpos.x, elytraFlight.getHoverTarget(), currentplayerBlockpos.getZ() - 40),
                        addlayer);
                break;
            case 5:
                //+x-z
                newpath = AstarAlg.findPath(startpath,//+- / -+ diagonals are strange this somehow fixes it
                        new BlockPos(currentplayerBlockpos.getX() + 40, elytraFlight.getHoverTarget(), startplayerpos.z - Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() + 40))),
                        addlayer);
                break;
            case 6:
                //+x
                newpath = AstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() + 40, elytraFlight.getHoverTarget(), startplayerpos.z),
                        addlayer);
                break;
            case 7:
                //+x+z
                newpath = AstarAlg.findPath(startpath,
                        new BlockPos(currentplayerBlockpos.getX() + 40, elytraFlight.getHoverTarget(), startplayerpos.z + Math.abs(startplayerpos.x - (currentplayerBlockpos.getX() + 40))),
                        addlayer);
                break;
        }
        if (continuePath) {
            newpath = reducePath(newpath);
            newpath.remove(0);
            path.addAll(newpath);
        } else {
            path = newpath;
        }
        //path= reducePath(path);
    }

    private static double calcPathLength() {
        if (path == null || path.size() < 2) return 0;
        double sum = Math.sqrt(Math.pow(mc.player.posX - path.get(0).pos.getX(), 2) + Math.pow(mc.player.posZ - path.get(0).pos.getZ(), 2));
        for (int i = 0; i < path.size() - 1; i++) {
            sum += Math.sqrt(Math.pow(path.get(i).pos.getX() - path.get(i + 1).pos.getX(), 2) + Math.pow(path.get(i).pos.getZ() - path.get(i + 1).pos.getZ(), 2));
        }
        return sum;

    }

    private static List<AstarNode> reducePath(List<AstarNode> original) {
        //not in use atm but prob later
        //combines multiple straight sections into one
        if (original.size() <= 3)
            return original;
        List<AstarNode> result = new ArrayList<>();
        int lastxdif = Integer.signum(original.get(0).pos.getX() - original.get(1).pos.getX());
        int lastzdif = Integer.signum(original.get(0).pos.getZ() - original.get(1).pos.getZ());
        int curxdif;
        int curzdif;
        result.add(original.get(0));
        for (int i = 1; i < original.size() - 1; i++) {
            curxdif = Integer.signum(original.get(i).pos.getX() - original.get(i + 1).pos.getX());
            curzdif = Integer.signum(original.get(i).pos.getZ() - original.get(i + 1).pos.getZ());
            if (curxdif != lastxdif || curzdif != lastzdif) {
                result.add(original.get(i));
            }
            lastxdif = curxdif;
            lastzdif = curzdif;
        }
        result.add(original.get(original.size() - 1));
        return result;
    }

    private static void gotoTrack() {

        switch (buildstate) {
            case GOTO_BUILD:
                if (!BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive()) {
                    if (!baritonePathStarted) {
                        takeoffPos = findClosestTunnel();
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(takeoffPos));
                        baritonePathStarted = true;
                    } else {
                        baritonePathStarted = false;
                        buildstate = TrackState.MINE_RADIUS;
                    }
                }
                break;
            case MINE_RADIUS:
                if (!BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isActive()) {
                    Iterable<BlockPos> blocks = BlockPos.getAllInBox(takeoffPos.add(-1, 0, -1), takeoffPos.add(1, 2, 1));
                    switch (currentDirection) {
                        case 0:
                            //+z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(0, 0, 3));
                            break;
                        case 1:
                            //-x+z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(-1, 0, +1));
                            break;
                        case 2:
                            //-x
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(-3, 0, 0));
                            break;
                        case 3:
                            //-x-z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(-1, 0, -1));
                            break;
                        case 4:
                            //-z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(0, 0, -3));
                            break;
                        case 5:
                            //+x-z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(+1, 0, -1));
                            break;
                        case 6:
                            //+x
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(3, 0, 0));
                            break;
                        case 7:
                            //+x+z
                            blocks = BlockPos.getAllInBox(takeoffPos.add(0, 2, 0), takeoffPos.add(1, 0, 1));
                            break;
                        default:
                            break;

                    }
                    for (BlockPos p : blocks) {
                        Block b = mc.world.getBlockState(p).getBlock();
                        if (!Blocks.BEDROCK.equals(b) && !Blocks.AIR.equals(b) && !Blocks.PORTAL.equals(b) && !Blocks.LAVA.equals(b) && !Blocks.FLOWING_LAVA.equals(b)) {
                            BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().clearArea(p, p);
                            return;
                        }
                    }
                    buildstate = TrackState.GOTO_TAKEOFF;
                }
                break;
            case GOTO_TAKEOFF:
                if (!BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive()) {
                    if (!baritonePathStarted) {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(takeoffPos));
                        baritonePathStarted = true;
                    } else {

                        baritonePathStarted = false;
                        buildstate = TrackState.CENTER_ON_BLOCK;

                    }
                }
                break;
            case CENTER_ON_BLOCK:
                if (new Vec3d(takeoffPos.getX() + 0.5, 0, takeoffPos.getZ() + 0.5).distanceTo(new Vec3d(mc.player.posX, 0, mc.player.posZ)) > 0.2) {
                    SurroundUtils.centerPlayer(false);
                } else {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                    mc.player.rotationPitch = 0;
                    mc.player.rotationYaw = currentDirection <= 4 ? currentDirection * 45 : -180 + currentDirection % 4 * 45;
                    baritonePathStarted = false;
                    buildstate = TrackState.GOTO_BUILD;
                    elytraFlight.setHoverTarget(Math.floor(takeoffPos.y) + 1.3);
                    state = State.START_FLYING;
                }
                break;
        }

    }


    private static BlockPos findClosestTunnel() {
        final BlockPos target;
        List<Vec3i> tunnel;
        switch (currentDirection) {
            case 0:
                //+z
                target = new BlockPos(startplayerpos.x, startplayerpos.y,
                        getPlayerBlockpos().getZ() + 10);
                tunnel = Tunnels.TUNNEL_Z;
                break;
            case 1:
                //-x+z
                target = new BlockPos(getPlayerBlockpos().getX() - 10, startplayerpos.y,
                        startplayerpos.z + Math.abs(startplayerpos.x - (getPlayerBlockpos().getX() - 10)));
                tunnel = Tunnels.TUNNEL_POS_NEG;
                break;
            case 2:
                //-x
                target = new BlockPos(getPlayerBlockpos().getX() - 10, startplayerpos.y,
                        startplayerpos.z);
                tunnel = Tunnels.TUNNEL_X;
                break;
            case 3:
                //-x-z
                target = new BlockPos(getPlayerBlockpos().getX() - 10, startplayerpos.y,
                        startplayerpos.z - Math.abs(startplayerpos.x - (getPlayerBlockpos().getX() - 10)));
                tunnel = Tunnels.TUNNEL_POS_POS;
                break;
            case 4:
                //-z
                target = new BlockPos(startplayerpos.x, startplayerpos.y,
                        getPlayerBlockpos().getZ() - 10);
                tunnel = Tunnels.TUNNEL_Z;
                break;
            case 5:
                //+x-z
                target = new BlockPos(getPlayerBlockpos().getX() + 10, startplayerpos.y,
                        startplayerpos.z - Math.abs(startplayerpos.x - (getPlayerBlockpos().getX() + 10)));
                tunnel = Tunnels.TUNNEL_POS_NEG;
                break;
            case 6:
                //+x
                target = new BlockPos(getPlayerBlockpos().getX() + 10, startplayerpos.y,
                        startplayerpos.z);
                tunnel = Tunnels.TUNNEL_X;
                break;
            case 7:
                //+x+z
                target = new BlockPos(getPlayerBlockpos().getX() + 10, startplayerpos.y, startplayerpos.z + Math.abs(startplayerpos.x - (getPlayerBlockpos().getX() + 10)));
                tunnel = Tunnels.TUNNEL_POS_POS;
                break;
            default:
                target = getPlayerBlockpos();
                tunnel = Tunnels.TUNNEL_X;
                break;
        }
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(new BlockPos(target.x - 20, Math.max(3, target.y - 10), target.z - 20), new BlockPos(target.x + 20, Math.min(roof, target.y + 10), target.z + 20));
        List<BlockPos> blocklist = new ArrayList<>();
        blocks.iterator().forEachRemaining(blocklist::add);
        blocklist.sort(Comparator.comparingDouble(o -> Math.pow(target.x - o.x, 2) + Math.pow((target.y - o.y) * 3, 2) + Math.pow(target.z - o.z, 2)));
        for (BlockPos b : blocklist) {
            if (checkTunnelAt(b, tunnel)) {
                return b;
            }
        }
        MessageSendHelper.sendChatMessage("No tunnel Found");
        return target;

    }

    private static boolean checkTunnelAt(BlockPos center, List<Vec3i> tunnel) {
        boolean blocked = true;
        if (Blocks.BEDROCK.equals(mc.world.getBlockState(center.add(0, 2, 0)).getBlock())
                || Blocks.LAVA.equals(mc.world.getBlockState(center.add(0, 3, 0)).getBlock())
                || Blocks.FLOWING_LAVA.equals(mc.world.getBlockState(center.add(0, 3, 0)).getBlock())
                || Blocks.LAVA.equals(mc.world.getBlockState(center.add(0, -1, 0)).getBlock())
                || Blocks.FLOWING_LAVA.equals(mc.world.getBlockState(center.add(0, -1, 0)).getBlock())
                || AstarNode.noclipblocks.contains(mc.world.getBlockState(center.add(0, -1, 0)).getBlock())
        ) {
            return false;
        }
        for (Vec3i v : tunnel) {
            if (!AstarNode.noclipblocks.contains(mc.world.getBlockState(center.add(v)).getBlock())) {
                blocked = false;
                break;
            }
        }
        return blocked;
    }

    private static BlockPos getPlayerBlockpos() {
        return new BlockPos((int) Math.floor(mc.player.posX), (int) Math.floor(mc.player.posY), (int) Math.floor(mc.player.posZ));
    }
}
