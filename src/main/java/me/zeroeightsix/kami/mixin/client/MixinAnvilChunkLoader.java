package me.zeroeightsix.kami.mixin.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnvilChunkLoader.class)
public interface MixinAnvilChunkLoader {
    @Invoker
    void invokeWriteChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound compound);
}
