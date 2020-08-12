// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.event.events;

import net.minecraft.entity.Entity;
import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import me.zeroeightsix.kami.event.KamiEvent;

public class AddCollisionBoxToListEvent extends KamiEvent
{
    private final Block block;
    private final IBlockState state;
    private final World world;
    private final BlockPos pos;
    private final AxisAlignedBB entityBox;
    private final List<AxisAlignedBB> collidingBoxes;
    private final Entity entity;
    private final boolean bool;
    
    public AddCollisionBoxToListEvent(final Block block, final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final Entity entityIn, final boolean bool) {
        this.block = block;
        this.state = state;
        this.world = worldIn;
        this.pos = pos;
        this.entityBox = entityBox;
        this.collidingBoxes = collidingBoxes;
        this.entity = entityIn;
        this.bool = bool;
    }
    
    public Block getBlock() {
        return this.block;
    }
    
    public IBlockState getState() {
        return this.state;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public BlockPos getPos() {
        return this.pos;
    }
    
    public AxisAlignedBB getEntityBox() {
        return this.entityBox;
    }
    
    public List<AxisAlignedBB> getCollidingBoxes() {
        return this.collidingBoxes;
    }
    
    public Entity getEntity() {
        return this.entity;
    }
    
    public boolean isBool() {
        return this.bool;
    }
}
