// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.entity.Entity;
import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.util.TrajectoryCalculator;
import net.minecraft.entity.EntityLivingBase;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.util.HueCycler;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Trajectories", category = Category.RENDER)
public class Trajectories extends Module
{
    ArrayList<Vec3d> positions;
    HueCycler cycler;
    
    public Trajectories() {
        this.positions = new ArrayList<Vec3d>();
        this.cycler = new HueCycler(100);
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        try {
            final TrajectoryCalculator.ThrowingType tt;
            TrajectoryCalculator.FlightPath flightPath;
            BlockPos hit;
            Vec3d a;
            final Iterator<Vec3d> iterator;
            Vec3d v;
            Trajectories.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityLivingBase).map(entity -> entity).forEach(entity -> {
                this.positions.clear();
                tt = TrajectoryCalculator.getThrowType(entity);
                if (tt != TrajectoryCalculator.ThrowingType.NONE) {
                    flightPath = new TrajectoryCalculator.FlightPath(entity, tt);
                    while (!flightPath.isCollided()) {
                        flightPath.onUpdate();
                        this.positions.add(flightPath.position);
                    }
                    hit = null;
                    if (flightPath.getCollidingTarget() != null) {
                        hit = flightPath.getCollidingTarget().func_178782_a();
                    }
                    GL11.glEnable(3042);
                    GL11.glDisable(3553);
                    GL11.glDisable(2896);
                    GL11.glDisable(2929);
                    if (hit != null) {
                        KamiTessellator.prepare(7);
                        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);
                        KamiTessellator.drawBox(hit, 872415231, GeometryMasks.FACEMAP.get(flightPath.getCollidingTarget().field_178784_b));
                        KamiTessellator.release();
                    }
                    if (!this.positions.isEmpty()) {
                        GL11.glDisable(3042);
                        GL11.glDisable(3553);
                        GL11.glDisable(2896);
                        GL11.glLineWidth(2.0f);
                        if (hit != null) {
                            GL11.glColor3f(1.0f, 1.0f, 1.0f);
                        }
                        else {
                            this.cycler.setNext();
                        }
                        GL11.glBegin(1);
                        a = this.positions.get(0);
                        GL11.glVertex3d(a.field_72450_a - Trajectories.mc.func_175598_ae().field_78725_b, a.field_72448_b - Trajectories.mc.func_175598_ae().field_78726_c, a.field_72449_c - Trajectories.mc.func_175598_ae().field_78723_d);
                        this.positions.iterator();
                        while (iterator.hasNext()) {
                            v = iterator.next();
                            GL11.glVertex3d(v.field_72450_a - Trajectories.mc.func_175598_ae().field_78725_b, v.field_72448_b - Trajectories.mc.func_175598_ae().field_78726_c, v.field_72449_c - Trajectories.mc.func_175598_ae().field_78723_d);
                            GL11.glVertex3d(v.field_72450_a - Trajectories.mc.func_175598_ae().field_78725_b, v.field_72448_b - Trajectories.mc.func_175598_ae().field_78726_c, v.field_72449_c - Trajectories.mc.func_175598_ae().field_78723_d);
                            if (hit == null) {
                                this.cycler.setNext();
                            }
                        }
                        GL11.glEnd();
                        GL11.glEnable(3042);
                        GL11.glEnable(3553);
                        this.cycler.reset();
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
