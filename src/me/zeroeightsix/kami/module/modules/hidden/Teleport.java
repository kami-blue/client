// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.hidden;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Teleport", description = "Library for teleport command", category = Category.HIDDEN)
public class Teleport extends Module
{
    private long lastTp;
    private Vec3d lastPos;
    public static Vec3d finalPos;
    public static double blocksPerTeleport;
    
    @Override
    public void onUpdate() {
        if (Teleport.finalPos == null) {
            Command.sendErrorMessage("Position not set, use .tp");
            this.disable();
            return;
        }
        final Vec3d tpDirectionVec = Teleport.finalPos.func_178786_a(Teleport.mc.field_71439_g.field_70165_t, Teleport.mc.field_71439_g.field_70163_u, Teleport.mc.field_71439_g.field_70161_v).func_72432_b();
        if (Teleport.mc.field_71441_e.func_175667_e(Teleport.mc.field_71439_g.func_180425_c())) {
            this.lastPos = new Vec3d(Teleport.mc.field_71439_g.field_70165_t, Teleport.mc.field_71439_g.field_70163_u, Teleport.mc.field_71439_g.field_70161_v);
            if (Teleport.finalPos.func_72438_d(new Vec3d(Teleport.mc.field_71439_g.field_70165_t, Teleport.mc.field_71439_g.field_70163_u, Teleport.mc.field_71439_g.field_70161_v)) < 0.3 || Teleport.blocksPerTeleport == 0.0) {
                Command.sendChatMessage("Teleport Finished!");
                this.disable();
            }
            else {
                Teleport.mc.field_71439_g.func_70016_h(0.0, 0.0, 0.0);
            }
            if (Teleport.finalPos.func_72438_d(new Vec3d(Teleport.mc.field_71439_g.field_70165_t, Teleport.mc.field_71439_g.field_70163_u, Teleport.mc.field_71439_g.field_70161_v)) >= Teleport.blocksPerTeleport) {
                final Vec3d vec = tpDirectionVec.func_186678_a(Teleport.blocksPerTeleport);
                Teleport.mc.field_71439_g.func_70107_b(Teleport.mc.field_71439_g.field_70165_t + vec.field_72450_a, Teleport.mc.field_71439_g.field_70163_u + vec.field_72448_b, Teleport.mc.field_71439_g.field_70161_v + vec.field_72449_c);
            }
            else {
                final Vec3d vec = tpDirectionVec.func_186678_a(Teleport.finalPos.func_72438_d(new Vec3d(Teleport.mc.field_71439_g.field_70165_t, Teleport.mc.field_71439_g.field_70163_u, Teleport.mc.field_71439_g.field_70161_v)));
                Teleport.mc.field_71439_g.func_70107_b(Teleport.mc.field_71439_g.field_70165_t + vec.field_72450_a, Teleport.mc.field_71439_g.field_70163_u + vec.field_72448_b, Teleport.mc.field_71439_g.field_70161_v + vec.field_72449_c);
                this.disable();
            }
            this.lastTp = System.currentTimeMillis();
        }
        else if (this.lastTp + 2000L > System.currentTimeMillis()) {
            Teleport.mc.field_71439_g.func_70107_b(this.lastPos.field_72450_a, this.lastPos.field_72448_b, this.lastPos.field_72449_c);
        }
    }
}
