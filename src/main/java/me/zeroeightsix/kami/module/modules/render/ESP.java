package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 14/12/2017.
 * Updated by d1gress/Qther on 27/11/2019.
 */
@Module.Info(name = "ESP", category = Module.Category.RENDER)
public class ESP extends Module {

    private Setting<ESPMode> mode = register(Settings.e("Mode", ESPMode.RECTANGLE));
    private Setting<Boolean> players = register(Settings.b("Players", true));
    private Setting<Boolean> animals = register(Settings.b("Animals", false));
    private Setting<Boolean> mobs = register(Settings.b("Mobs", false));

    public enum ESPMode {
        RECTANGLE,
        GLOW
    }

    @Override
    public void onWorldRender(RenderEvent event) {


        if (Wrapper.getMinecraft().getRenderManager().options == null) return;
        switch (mode.getValue()) {
            case RECTANGLE:
                boolean isThirdPersonFrontal = Wrapper.getMinecraft().getRenderManager().options.thirdPersonView == 2;
                float viewerYaw = Wrapper.getMinecraft().getRenderManager().playerViewY;

                mc.world.loadedEntityList.stream()
                        .filter(EntityUtil::isLiving)
                        .filter(entity -> mc.player != entity)
                        .map(entity -> (EntityLivingBase) entity)
                        .filter(entityLivingBase -> !entityLivingBase.isDead)
                        .filter(entity -> (players.getValue() && entity instanceof EntityPlayer) || (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue()))
                        .forEach(e -> {
                            GlStateManager.pushMatrix();
                            Vec3d pos = EntityUtil.getInterpolatedPos(e, event.getPartialTicks());
                            GlStateManager.translate(pos.x-mc.getRenderManager().renderPosX, pos.y-mc.getRenderManager().renderPosY, pos.z-mc.getRenderManager().renderPosZ);
                            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1), 1.0F, 0.0F, 0.0F);
                            GlStateManager.disableLighting();
                            GlStateManager.depthMask(false);

                            GlStateManager.disableDepth();

                            GlStateManager.enableBlend();
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                            if (e instanceof EntityPlayer) glColor3f(1,1,1);
                            else if (EntityUtil.isPassive(e)) glColor3f(0.11f,0.9f,0.11f);
                            else glColor3f(0.9f,.1f,.1f);

                            GlStateManager.disableTexture2D();
                            glLineWidth(2f);
                            glEnable( GL_LINE_SMOOTH );
                            glBegin(GL_LINE_LOOP);
                            {
                                glVertex2d(-e.width/2,0);
                                glVertex2d(-e.width/2,e.height);
                                glVertex2d(e.width/2,e.height);
                                glVertex2d(e.width/2,0);
                            }
                            glEnd();

                            GlStateManager.popMatrix();
                        });
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.disableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                GlStateManager.disableDepth();
                GlStateManager.enableCull();
                GlStateManager.glLineWidth(1);
                glColor3f(1,1,1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(ESPMode.GLOW)) {
            for (Entity e : mc.world.loadedEntityList) {
                if (e == null || e.isDead) return;
                if (e instanceof EntityPlayer && players.getValue() && !e.isGlowing()) {
                    e.setGlowing(true);
                } else if (e instanceof EntityPlayer && !players.getValue() && e.isGlowing()) {
                    e.setGlowing(false);
                }
                if (EntityUtil.isHostileMob(e) && mobs.getValue() && !e.isGlowing()) {
                    e.setGlowing(true);
                } else if (EntityUtil.isHostileMob(e) && !mobs.getValue() && e.isGlowing()) {
                    e.setGlowing(false);
                }
                if (EntityUtil.isPassive(e) && animals.getValue() && !e.isGlowing()) {
                    e.setGlowing(true);
                } else if (EntityUtil.isPassive(e) && !animals.getValue() && e.isGlowing()) {
                    e.setGlowing(false);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (mode.getValue().equals(ESPMode.GLOW)) {
            for (Entity e : mc.world.loadedEntityList) {
                e.setGlowing(false);
            }
            mc.player.setGlowing(false);
        }
    }
}
