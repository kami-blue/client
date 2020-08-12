// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import java.util.Iterator;
import java.util.Comparator;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.event.events.RenderEvent;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.WalkNodeProcessor;
import java.util.Collection;
import java.util.Arrays;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.World;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.pathfinding.PathPoint;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Pathfind", category = Category.MISC)
public class Pathfind extends Module
{
    public static ArrayList<PathPoint> points;
    static PathPoint to;
    
    public static boolean createPath(final PathPoint end) {
        Pathfind.to = end;
        final WalkNodeProcessor walkNodeProcessor = new AnchoredWalkNodeProcessor(new PathPoint((int)Pathfind.mc.field_71439_g.field_70165_t, (int)Pathfind.mc.field_71439_g.field_70163_u, (int)Pathfind.mc.field_71439_g.field_70161_v));
        final EntityZombie zombie = new EntityZombie((World)Pathfind.mc.field_71441_e);
        zombie.func_184644_a(PathNodeType.WATER, 16.0f);
        zombie.field_70165_t = Pathfind.mc.field_71439_g.field_70165_t;
        zombie.field_70163_u = Pathfind.mc.field_71439_g.field_70163_u;
        zombie.field_70161_v = Pathfind.mc.field_71439_g.field_70161_v;
        final PathFinder finder = new PathFinder((NodeProcessor)walkNodeProcessor);
        final Path path = finder.func_186336_a((IBlockAccess)Pathfind.mc.field_71441_e, (EntityLiving)zombie, new BlockPos(end.field_75839_a, end.field_75837_b, end.field_75838_c), Float.MAX_VALUE);
        zombie.func_184644_a(PathNodeType.WATER, 0.0f);
        if (path == null) {
            Command.sendChatMessage("Failed to create path!");
            return false;
        }
        Pathfind.points = new ArrayList<PathPoint>(Arrays.asList(path.field_75884_a));
        return Pathfind.points.get(Pathfind.points.size() - 1).func_75829_a(end) <= 1.0f;
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (Pathfind.points.isEmpty()) {
            return;
        }
        GL11.glDisable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        GL11.glLineWidth(1.5f);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GlStateManager.func_179097_i();
        GL11.glBegin(1);
        final PathPoint first = Pathfind.points.get(0);
        GL11.glVertex3d(first.field_75839_a - Pathfind.mc.func_175598_ae().field_78725_b + 0.5, first.field_75837_b - Pathfind.mc.func_175598_ae().field_78726_c, first.field_75838_c - Pathfind.mc.func_175598_ae().field_78723_d + 0.5);
        for (int i = 0; i < Pathfind.points.size() - 1; ++i) {
            final PathPoint pathPoint = Pathfind.points.get(i);
            GL11.glVertex3d(pathPoint.field_75839_a - Pathfind.mc.func_175598_ae().field_78725_b + 0.5, pathPoint.field_75837_b - Pathfind.mc.func_175598_ae().field_78726_c, pathPoint.field_75838_c - Pathfind.mc.func_175598_ae().field_78723_d + 0.5);
            if (i != Pathfind.points.size() - 1) {
                GL11.glVertex3d(pathPoint.field_75839_a - Pathfind.mc.func_175598_ae().field_78725_b + 0.5, pathPoint.field_75837_b - Pathfind.mc.func_175598_ae().field_78726_c, pathPoint.field_75838_c - Pathfind.mc.func_175598_ae().field_78723_d + 0.5);
            }
        }
        GL11.glEnd();
        GlStateManager.func_179126_j();
    }
    
    @Override
    public void onUpdate() {
        final PathPoint closest = Pathfind.points.stream().min(Comparator.comparing(pathPoint -> Pathfind.mc.field_71439_g.func_70011_f((double)pathPoint.field_75839_a, (double)pathPoint.field_75837_b, (double)pathPoint.field_75838_c))).orElse(null);
        if (closest == null) {
            return;
        }
        if (Pathfind.mc.field_71439_g.func_70011_f((double)closest.field_75839_a, (double)closest.field_75837_b, (double)closest.field_75838_c) > 0.8) {
            return;
        }
        final Iterator<PathPoint> iterator = Pathfind.points.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == closest) {
                iterator.remove();
                break;
            }
            iterator.remove();
        }
        if (Pathfind.points.size() <= 1 && Pathfind.to != null) {
            final boolean b = createPath(Pathfind.to);
            final boolean flag = Pathfind.points.size() <= 4;
            if ((b && flag) || flag) {
                Pathfind.points.clear();
                Pathfind.to = null;
                if (b) {
                    Command.sendChatMessage("Arrived!");
                }
                else {
                    Command.sendChatMessage("Can't go on: pathfinder has hit dead end");
                }
            }
        }
    }
    
    static {
        Pathfind.points = new ArrayList<PathPoint>();
        Pathfind.to = null;
    }
    
    private static class AnchoredWalkNodeProcessor extends WalkNodeProcessor
    {
        PathPoint from;
        
        public AnchoredWalkNodeProcessor(final PathPoint from) {
            this.from = from;
        }
        
        public PathPoint func_186318_b() {
            return this.from;
        }
        
        public boolean func_186323_c() {
            return true;
        }
        
        public boolean func_186322_e() {
            return true;
        }
        
        public PathNodeType func_186330_a(final IBlockAccess blockaccessIn, final int x, final int y, final int z) {
            PathNodeType pathnodetype = this.func_189553_b(blockaccessIn, x, y, z);
            if (pathnodetype == PathNodeType.OPEN && y >= 1) {
                final Block block = blockaccessIn.func_180495_p(new BlockPos(x, y - 1, z)).func_177230_c();
                final PathNodeType pathnodetype2 = this.func_189553_b(blockaccessIn, x, y - 1, z);
                pathnodetype = ((pathnodetype2 != PathNodeType.WALKABLE && pathnodetype2 != PathNodeType.OPEN && pathnodetype2 != PathNodeType.LAVA) ? PathNodeType.WALKABLE : PathNodeType.OPEN);
                if (pathnodetype2 == PathNodeType.DAMAGE_FIRE || block == Blocks.field_189877_df) {
                    pathnodetype = PathNodeType.DAMAGE_FIRE;
                }
                if (pathnodetype2 == PathNodeType.DAMAGE_CACTUS) {
                    pathnodetype = PathNodeType.DAMAGE_CACTUS;
                }
            }
            pathnodetype = this.func_193578_a(blockaccessIn, x, y, z, pathnodetype);
            return pathnodetype;
        }
        
        protected PathNodeType func_189553_b(final IBlockAccess p_189553_1_, final int p_189553_2_, final int p_189553_3_, final int p_189553_4_) {
            final BlockPos blockpos = new BlockPos(p_189553_2_, p_189553_3_, p_189553_4_);
            final IBlockState iblockstate = p_189553_1_.func_180495_p(blockpos);
            final Block block = iblockstate.func_177230_c();
            final Material material = iblockstate.func_185904_a();
            final PathNodeType type = block.getAiPathNodeType(iblockstate, p_189553_1_, blockpos);
            if (type != null) {
                return type;
            }
            if (material == Material.field_151579_a) {
                return PathNodeType.OPEN;
            }
            if (block == Blocks.field_150415_aT || block == Blocks.field_180400_cw || block == Blocks.field_150392_bi) {
                return PathNodeType.TRAPDOOR;
            }
            if (block == Blocks.field_150480_ab) {
                return PathNodeType.DAMAGE_FIRE;
            }
            if (block == Blocks.field_150434_aF) {
                return PathNodeType.DAMAGE_CACTUS;
            }
            if (block instanceof BlockDoor && material == Material.field_151575_d && !(boolean)iblockstate.func_177229_b((IProperty)BlockDoor.field_176519_b)) {
                return PathNodeType.DOOR_WOOD_CLOSED;
            }
            if (block instanceof BlockDoor && material == Material.field_151573_f && !(boolean)iblockstate.func_177229_b((IProperty)BlockDoor.field_176519_b)) {
                return PathNodeType.DOOR_IRON_CLOSED;
            }
            if (block instanceof BlockDoor && (boolean)iblockstate.func_177229_b((IProperty)BlockDoor.field_176519_b)) {
                return PathNodeType.DOOR_OPEN;
            }
            if (block instanceof BlockRailBase) {
                return PathNodeType.RAIL;
            }
            if (block instanceof BlockFence || block instanceof BlockWall || (block instanceof BlockFenceGate && !(boolean)iblockstate.func_177229_b((IProperty)BlockFenceGate.field_176466_a))) {
                return PathNodeType.FENCE;
            }
            if (material == Material.field_151586_h) {
                return PathNodeType.WALKABLE;
            }
            if (material == Material.field_151587_i) {
                return PathNodeType.LAVA;
            }
            return block.func_176205_b(p_189553_1_, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
        }
    }
}
