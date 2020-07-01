package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/**
 * @author 086
 */
@Module.Info(
        name = "EyeFinder",
        description = "Draw lines from entity's heads to where they are looking",
        category = Module.Category.RENDER
)
public class EyeFinder extends Module {

    private Setting<Boolean> players = register(Settings.b("Players", true));
    private Setting<Boolean> mobs = register(Settings.b("Mobs", false));
    private Setting<Boolean> passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility(v -> mobs.getValue()).build());
    private Setting<Boolean> neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility(v -> mobs.getValue()).build());
    private Setting<Boolean> hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility(v -> mobs.getValue()).build());

    @Override
    public void onWorldRender(RenderEvent event) {
        mc.world.loadedEntityList.stream()
                .filter(EntityUtil::isLiving)
                .filter(entity -> mc.player != entity)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entityLivingBase -> !entityLivingBase.isDead)
                .filter(entity -> (players.getValue() && entity instanceof EntityPlayer) || (EntityUtil.mobTypeSettings(entity, mobs.getValue(), passive.getValue(), neutral.getValue(), hostile.getValue())))
                .forEach(this::drawLine);
    }

    private void drawLine(EntityLivingBase e) {
        RayTraceResult result = e.rayTrace(6, Minecraft.getMinecraft().getRenderPartialTicks());
        if (result == null) return;
        Vec3d eyes = e.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks());

        GlStateManager.enableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        double posx = eyes.x - mc.getRenderManager().renderPosX;
        double posy = eyes.y - mc.getRenderManager().renderPosY;
        double posz = eyes.z - mc.getRenderManager().renderPosZ;
        double posx2 = result.hitVec.x - mc.getRenderManager().renderPosX;
        double posy2 = result.hitVec.y - mc.getRenderManager().renderPosY;
        double posz2 = result.hitVec.z - mc.getRenderManager().renderPosZ;
        GL11.glColor4f(.2f, .1f, .3f, .8f);
        GlStateManager.glLineWidth(1.5f);

        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex3d(posx, posy, posz);
            GL11.glVertex3d(posx2, posy2, posz2);
            GL11.glVertex3d(posx2, posy2, posz2);
            GL11.glVertex3d(posx2, posy2, posz2);
        }
        GL11.glEnd();

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            KamiTessellator.prepare(GL11.GL_QUADS);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            BlockPos b = result.getBlockPos();
            float x = b.x - .01f;
            float y = b.y - .01f;
            float z = b.z - .01f;
            KamiTessellator.drawBox(KamiTessellator.getBufferBuilder(), x, y, z, 1.01f, 1.01f, 1.01f, 51, 25, 73, 200, GeometryMasks.Quad.ALL);
            KamiTessellator.release();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
    }
}
