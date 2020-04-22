package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.HueCycler;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.TrajectoryCalculator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * Created by 086 on 28/12/2017.
 */
@Module.Info(
        name = "Trajectories",
        category = Module.Category.RENDER,
        description = "Draws lines to where trajectories are going to fall"
)
public class Trajectories extends Module {
    ArrayList<Vec3d> positions = new ArrayList<>();
    HueCycler cycler = new HueCycler(100);

    @Override
    public void onWorldRender(RenderEvent event) {
        try {
            mc.world.loadedEntityList.stream()
                    .filter(entity -> entity instanceof EntityLivingBase)
                    .map(entity -> (EntityLivingBase) entity)
                    .forEach(entity -> {
                        positions.clear();
                        TrajectoryCalculator.ThrowingType tt = TrajectoryCalculator.getThrowType(entity);
                        if (tt == TrajectoryCalculator.ThrowingType.NONE) return;
                        TrajectoryCalculator.FlightPath flightPath = new TrajectoryCalculator.FlightPath(entity, tt);

                        while (!flightPath.isCollided()) {
                            flightPath.onUpdate();
                            positions.add(flightPath.position);
                        }

                        BlockPos hit = null;
                        if (flightPath.getCollidingTarget() != null)
                            hit = flightPath.getCollidingTarget().getBlockPos();

                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        if (hit != null) {
                            KamiTessellator.prepare(GL11.GL_QUADS);
                            GL11.glColor4f(1, 1, 1, .3f);
                            KamiTessellator.drawBox(hit, 0x33ffffff, GeometryMasks.FACEMAP.get(flightPath.getCollidingTarget().sideHit));
                            KamiTessellator.release();
                        }

                        if (positions.isEmpty()) return;
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_LIGHTING);

                        GL11.glLineWidth(2F);
                        if (hit != null)
                            GL11.glColor3f(1f, 1f, 1f);
                        else
                            cycler.setNext();
                        GL11.glBegin(GL11.GL_LINES);

                        Vec3d a = positions.get(0);
                        GL11.glVertex3d(a.x - mc.getRenderManager().renderPosX, a.y - mc.getRenderManager().renderPosY, a.z - mc.getRenderManager().renderPosZ);
                        for (Vec3d v : positions) {
                            GL11.glVertex3d(v.x - mc.getRenderManager().renderPosX, v.y - mc.getRenderManager().renderPosY, v.z - mc.getRenderManager().renderPosZ);
                            GL11.glVertex3d(v.x - mc.getRenderManager().renderPosX, v.y - mc.getRenderManager().renderPosY, v.z - mc.getRenderManager().renderPosZ);
                            if (hit == null)
                                cycler.setNext();
                        }

                        GL11.glEnd();
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glEnable(GL11.GL_TEXTURE_2D);

                        cycler.reset();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
