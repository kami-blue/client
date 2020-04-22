package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by 086 on 25/01/2018.
 */

@Module.Info(
        name = "Pathfind",
        category = Module.Category.MISC,
        description = "A path finder for AutoWalk"
)
public class Pathfind extends Module {

    public static ArrayList<PathPoint> points = new ArrayList<>();
    static PathPoint to = null;

    public static boolean createPath(PathPoint end) {
        to = end;
        WalkNodeProcessor walkNodeProcessor = new AnchoredWalkNodeProcessor(new PathPoint((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ));
        EntityZombie zombie = new EntityZombie(mc.world);
        zombie.setPathPriority(PathNodeType.WATER, 16f);
        zombie.posX = mc.player.posX;
        zombie.posY = mc.player.posY;
        zombie.posZ = mc.player.posZ;

        PathFinder finder = new PathFinder(walkNodeProcessor);
        Path path = finder.findPath(mc.world, zombie, new BlockPos(end.x, end.y, end.z), Float.MAX_VALUE);
        zombie.setPathPriority(PathNodeType.WATER, 0);
        if (path == null) {
            sendChatMessage("Failed to create path!");
            return false;
        }
        points = new ArrayList<>(Arrays.asList(path.points));
        return points.get(points.size() - 1).distanceTo(end) <= 1; // Return whether or not the last path location is our end destination
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (points.isEmpty()) return;
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(1.5f);
        GL11.glColor3f(1, 1, 1);
        GlStateManager.disableDepth();

        GL11.glBegin(GL11.GL_LINES);
        PathPoint first = points.get(0);
        GL11.glVertex3d(first.x - mc.getRenderManager().renderPosX + .5, first.y - mc.getRenderManager().renderPosY, first.z - mc.getRenderManager().renderPosZ + .5);
        for (int i = 0; i < points.size() - 1; i++) {
            PathPoint pathPoint = points.get(i);
            GL11.glVertex3d(pathPoint.x - mc.getRenderManager().renderPosX + .5, pathPoint.y - mc.getRenderManager().renderPosY, pathPoint.z - mc.getRenderManager().renderPosZ + .5);
            if (i != points.size() - 1) {
                GL11.glVertex3d(pathPoint.x - mc.getRenderManager().renderPosX + .5, pathPoint.y - mc.getRenderManager().renderPosY, pathPoint.z - mc.getRenderManager().renderPosZ + .5);
            }
        }
        GL11.glEnd();

        GlStateManager.enableDepth();
    }

    @Override
    public void onUpdate() {
        PathPoint closest = points.stream().min(Comparator.comparing(pathPoint -> mc.player.getDistance(pathPoint.x, pathPoint.y, pathPoint.z))).orElse(null);
        if (closest == null) return;
        if (mc.player.getDistance(closest.x, closest.y, closest.z) > .8) return;
        Iterator<PathPoint> iterator = points.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == closest) {
                iterator.remove();
                break;
            }
            iterator.remove();
        }

        if ((points.size() <= 1) && to != null) {
            boolean b = createPath(to);
            boolean flag = points.size() <= 4;
            if ((b && flag) || flag) {    // The only points present are the starting and end point (or <=2 points in between). We've arrived!
                // Might also return true if we've hit a dead end
                points.clear();
                to = null;
                if (b)
                    sendChatMessage("Arrived!");
                else
                    sendChatMessage("Can't go on: pathfinder has hit dead end");
            }
        }
    }

    private static class AnchoredWalkNodeProcessor extends WalkNodeProcessor {

        PathPoint from;

        public AnchoredWalkNodeProcessor(PathPoint from) {
            this.from = from;
        }

        @Override
        public PathPoint getStart() {
            return from;
        }

        @Override
        public boolean getCanEnterDoors() {
            return true;
        }

        @Override
        public boolean getCanSwim() {
            return true;
        }

        @Override
        public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z) {
            PathNodeType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);

            if (pathnodetype == PathNodeType.OPEN && y >= 1) {
                Block block = blockaccessIn.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
                PathNodeType pathnodetype1 = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
                pathnodetype =
                        pathnodetype1 != PathNodeType.WALKABLE
                                && pathnodetype1 != PathNodeType.OPEN
                                && pathnodetype1 != PathNodeType.LAVA
                                ? PathNodeType.WALKABLE : PathNodeType.OPEN;

                if (pathnodetype1 == PathNodeType.DAMAGE_FIRE || block == Blocks.MAGMA) {
                    pathnodetype = PathNodeType.DAMAGE_FIRE;
                }

                if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
                    pathnodetype = PathNodeType.DAMAGE_CACTUS;
                }
            }

            pathnodetype = this.checkNeighborBlocks(blockaccessIn, x, y, z, pathnodetype);
            return pathnodetype;
        }

        @Override
        protected PathNodeType getPathNodeTypeRaw(IBlockAccess p_189553_1_, int p_189553_2_, int p_189553_3_, int p_189553_4_) {
            BlockPos blockpos = new BlockPos(p_189553_2_, p_189553_3_, p_189553_4_);
            IBlockState iblockstate = p_189553_1_.getBlockState(blockpos);
            Block block = iblockstate.getBlock();
            Material material = iblockstate.getMaterial();

            PathNodeType type = block.getAiPathNodeType(iblockstate, p_189553_1_, blockpos);
            if (type != null) return type;

            if (material == Material.AIR) {
                return PathNodeType.OPEN;
            } else if (block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR && block != Blocks.WATERLILY) {
                if (block == Blocks.FIRE) {
                    return PathNodeType.DAMAGE_FIRE;
                } else if (block == Blocks.CACTUS) {
                    return PathNodeType.DAMAGE_CACTUS;
                } else if (block instanceof BlockDoor && material == Material.WOOD && !iblockstate.getValue(BlockDoor.OPEN)) {
                    return PathNodeType.DOOR_WOOD_CLOSED;
                } else if (block instanceof BlockDoor && material == Material.IRON && !iblockstate.getValue(BlockDoor.OPEN)) {
                    return PathNodeType.DOOR_IRON_CLOSED;
                } else if (block instanceof BlockDoor && iblockstate.getValue(BlockDoor.OPEN)) {
                    return PathNodeType.DOOR_OPEN;
                } else if (block instanceof BlockRailBase) {
                    return PathNodeType.RAIL;
                } else if (!(block instanceof BlockFence) && !(block instanceof BlockWall) && (!(block instanceof BlockFenceGate) || iblockstate.getValue(BlockFenceGate.OPEN))) {
                    if (material == Material.WATER) {
                        return PathNodeType.WALKABLE;
                    } else if (material == Material.LAVA) {
                        return PathNodeType.LAVA;
                    } else {
                        return block.isPassable(p_189553_1_, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
                    }
                } else {
                    return PathNodeType.FENCE;
                }
            } else {
                return PathNodeType.TRAPDOOR;
            }
        }
    }

}
