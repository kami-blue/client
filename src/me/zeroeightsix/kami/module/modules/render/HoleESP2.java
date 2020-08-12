// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.event.events.RenderEvent;
import java.util.Iterator;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "HoleESP2", category = Category.RENDER)
public class HoleESP2 extends Module
{
    private ArrayList<BlockPos> holes;
    private ArrayList<BlockPos> greenholes;
    private ArrayList<BlockPos> redholes;
    private Setting<Modes> modes;
    private Setting<Integer> range;
    private Setting<Integer> count;
    private Setting<Integer> red;
    private Setting<Integer> green;
    private Setting<Integer> blue;
    
    public HoleESP2() {
        this.modes = this.register(Settings.e("Modes", Modes.CUSTOM));
        this.range = this.register((Setting<Integer>)Settings.integerBuilder("Range").withRange(0, 15).withValue(8).build());
        this.count = this.register((Setting<Integer>)Settings.integerBuilder("Counts").withRange(0, 50).withValue(10).build());
        this.red = this.register((Setting<Integer>)Settings.integerBuilder("Red").withRange(0, 255).withValue(0).build());
        this.green = this.register((Setting<Integer>)Settings.integerBuilder("Green").withRange(0, 255).withValue(255).build());
        this.blue = this.register((Setting<Integer>)Settings.integerBuilder("Blue").withRange(0, 255).withValue(0).build());
    }
    
    @Override
    public void onUpdate() {
        int holecount = 0;
        this.holes = new ArrayList<BlockPos>();
        this.greenholes = new ArrayList<BlockPos>();
        this.redholes = new ArrayList<BlockPos>();
        final Iterable<BlockPos> blocks = (Iterable<BlockPos>)BlockPos.func_177980_a(HoleESP2.mc.field_71439_g.func_180425_c().func_177982_a(-this.range.getValue(), -6, -this.range.getValue()), HoleESP2.mc.field_71439_g.func_180425_c().func_177982_a((int)this.range.getValue(), 2, (int)this.range.getValue()));
        for (final BlockPos pos : blocks) {
            if (!HoleESP2.mc.field_71441_e.func_180495_p(pos).func_185904_a().func_76230_c() && !HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 1, 0)).func_185904_a().func_76230_c()) {
                final boolean solidNeighbours = (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_177230_c() == Blocks.field_150357_h | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_177230_c() == Blocks.field_150343_Z) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_177230_c() == Blocks.field_150357_h | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_177230_c() == Blocks.field_150343_Z) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_177230_c() == Blocks.field_150357_h | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_177230_c() == Blocks.field_150343_Z) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_177230_c() == Blocks.field_150357_h | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_177230_c() == Blocks.field_150343_Z) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_177230_c() == Blocks.field_150357_h | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_177230_c() == Blocks.field_150343_Z) && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 0)).func_185904_a() == Material.field_151579_a && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 1, 0)).func_185904_a() == Material.field_151579_a && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 2, 0)).func_185904_a() == Material.field_151579_a;
                final boolean obiNeighbours = (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_177230_c() == Blocks.field_150343_Z | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_177230_c() == Blocks.field_150343_Z | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_177230_c() == Blocks.field_150343_Z | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_177230_c() == Blocks.field_150343_Z | HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_177230_c() == Blocks.field_150343_Z) && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 0)).func_185904_a() == Material.field_151579_a && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 1, 0)).func_185904_a() == Material.field_151579_a && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 2, 0)).func_185904_a() == Material.field_151579_a;
                final boolean bedNeighbours = HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_177230_c() == Blocks.field_150357_h && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_177230_c() == Blocks.field_150357_h && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_177230_c() == Blocks.field_150357_h && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_177230_c() == Blocks.field_150357_h && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_177230_c() == Blocks.field_150357_h && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 0)).func_185904_a() == Material.field_151579_a && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 1, 0)).func_185904_a() == Material.field_151579_a && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 2, 0)).func_185904_a() == Material.field_151579_a;
                if (!solidNeighbours) {
                    continue;
                }
                if (holecount >= this.count.getValue()) {
                    continue;
                }
                if (obiNeighbours) {
                    this.redholes.add(pos);
                }
                if (bedNeighbours) {
                    this.greenholes.add(pos);
                }
                this.holes.add(pos);
                ++holecount;
            }
        }
    }
    
    @Override
    public String getHudInfo() {
        switch (this.modes.getValue()) {
            case CUSTOM: {
                return "custom";
            }
            case GAY: {
                return "gay";
            }
            default: {
                return "";
            }
        }
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.greenholes != null && this.redholes != null && this.holes != null) {
            switch (this.modes.getValue()) {
                case GAY: {
                    KamiTessellator.prepare(7);
                    this.greenholes.forEach(blockPos -> KamiTessellator.drawBox(blockPos, 0, 255, 0, 40, 63));
                    this.redholes.forEach(blockPos -> KamiTessellator.drawBox(blockPos, 255, 0, 0, 40, 63));
                    KamiTessellator.release();
                    break;
                }
                case CUSTOM: {
                    KamiTessellator.prepare(7);
                    this.holes.forEach(blockPos -> KamiTessellator.drawBox(blockPos, this.red.getValue(), this.green.getValue(), this.blue.getValue(), 40, 63));
                    KamiTessellator.release();
                    break;
                }
            }
        }
    }
    
    public boolean isHole(final BlockPos pos) {
        return this.isHole(pos, false);
    }
    
    public boolean isHole(final BlockPos pos, final boolean holeheight) {
        if (pos.func_177956_o() > 125.0) {
            return false;
        }
        final boolean isSolid = !HoleESP2.mc.field_71441_e.func_180495_p(pos).func_185904_a().func_76230_c() && !HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 1, 0)).func_185904_a().func_76230_c() && (!HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 2, 0)).func_185904_a().func_76230_c() || !holeheight) && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_185904_a().func_76220_a() && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_185904_a().func_76220_a() && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_185904_a().func_76220_a() && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_185904_a().func_76220_a() && HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_185904_a().func_76220_a();
        final boolean isBedrock = (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_177230_c().equals(Blocks.field_150357_h) || HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, -1, 0)).func_177230_c().equals(Blocks.field_150343_Z)) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_177230_c().equals(Blocks.field_150357_h) || HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(1, 0, 0)).func_177230_c().equals(Blocks.field_150343_Z)) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_177230_c().equals(Blocks.field_150357_h) || HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, 1)).func_177230_c().equals(Blocks.field_150343_Z)) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_177230_c().equals(Blocks.field_150357_h) || HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(-1, 0, 0)).func_177230_c().equals(Blocks.field_150343_Z)) && (HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_177230_c().equals(Blocks.field_150357_h) || HoleESP2.mc.field_71441_e.func_180495_p(pos.func_177982_a(0, 0, -1)).func_177230_c().equals(Blocks.field_150343_Z));
        return isBedrock || isSolid;
    }
    
    private enum Modes
    {
        CUSTOM, 
        GAY;
    }
}
