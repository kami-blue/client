// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.util.math.Vec3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.util.other.MathUtil;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.RayTraceResult;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Block-Highlight", category = Category.RENDER)
public class BlockHighlight extends Module
{
    private Setting<AppendModes> highlightMode;
    private Setting<Integer> red;
    private Setting<Integer> green;
    private Setting<Integer> blue;
    private Setting<Integer> alpha;
    private Setting<Float> width;
    
    public BlockHighlight() {
        this.red = this.register((Setting<Integer>)Settings.integerBuilder("Red").withRange(0, 255).withValue(0).build());
        this.green = this.register((Setting<Integer>)Settings.integerBuilder("Green").withRange(0, 255).withValue(0).build());
        this.blue = this.register((Setting<Integer>)Settings.integerBuilder("Blue").withRange(0, 255).withValue(255).build());
        this.alpha = this.register((Setting<Integer>)Settings.integerBuilder("Alpha").withRange(0, 255).withValue(70).build());
        this.width = this.register((Setting<Float>)Settings.floatBuilder("Width").withRange(0.0f, 10.0f).withValue(1.5f).build());
        this.highlightMode = this.register(Settings.e("Highlight Mode", AppendModes.FULLBOX));
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        final RayTraceResult ray = BlockHighlight.mc.field_71476_x;
        if (ray.field_72313_a == RayTraceResult.Type.BLOCK) {
            final BlockPos blockpos = ray.func_178782_a();
            final IBlockState iblockstate = BlockHighlight.mc.field_71441_e.func_180495_p(blockpos);
            if (iblockstate.func_185904_a() != Material.field_151579_a && BlockHighlight.mc.field_71441_e.func_175723_af().func_177746_a(blockpos)) {
                final Vec3d interp = MathUtil.interpolateEntity((Entity)BlockHighlight.mc.field_71439_g, BlockHighlight.mc.func_184121_ak());
                if (blockpos != null) {
                    switch (this.highlightMode.getValue()) {
                        case FULLBOX: {
                            KamiTessellator.prepare(7);
                            KamiTessellator.drawBox(blockpos, this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue(), 63);
                            KamiTessellator.release();
                            break;
                        }
                        case OUTLINE: {
                            KamiTessellator.drawBoundingBox(iblockstate.func_185918_c((World)BlockHighlight.mc.field_71441_e, blockpos).func_186662_g(0.0020000000949949026).func_72317_d(-interp.field_72450_a, -interp.field_72448_b, -interp.field_72449_c), this.width.getValue(), this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private enum AppendModes
    {
        FULLBOX, 
        OUTLINE;
    }
}
