package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.TrajectoryCalculator;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.ESPRenderer;
import me.zeroeightsix.kami.util.graphics.GeometryMasks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

/**
 * Created by 086 on 28/12/2017.
 * Updated by Xiaro on 31/07/20
 */
@Module.Info(
        name = "Trajectories",
        category = Module.Category.RENDER,
        description = "Draws lines to where trajectories are going to fall"
)
public class Trajectories extends Module {
    ArrayList<Vec3d> positions = new ArrayList<>();

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

                        if (hit != null) {
                            GlStateManager.pushMatrix();
                            ESPRenderer renderer = new ESPRenderer();
                            renderer.setAFilled(150);
                            renderer.setAOutline(0);
                            renderer.setThrough(false);
                            renderer.setThickness(0.0f);
                            renderer.setFullOutline(true);
                            AxisAlignedBB box = mc.world.getBlockState(hit).getSelectedBoundingBox(mc.world, hit);
                            renderer.add(box.grow(0.002), new ColorHolder(255, 255, 255), GeometryMasks.Quad.ALL);
                            renderer.render(true);
                            GlStateManager.popMatrix();
                        }

                        if (positions.isEmpty()) return;

/*                        GL11.glLineWidth(2F);
                        glColor3f(1f, 1f, 1f);
                        glBegin(GL_LINE_STRIP);
                        Vec3d a = positions.get(0);
                        glVertex3d(a.x, a.y, a.z);
                        for (Vec3d v : positions) {
                            glVertex3d(v.x, v.y, v.z);
                        }
                        glEnd();*/
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
