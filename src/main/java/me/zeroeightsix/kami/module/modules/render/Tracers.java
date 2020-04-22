package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.HueCycler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static me.zeroeightsix.kami.util.ColourConverter.rgbToInt;

/**
 * Created by 086 on 11/12/2017.
 * <p>
 * Kurisu Makise is best girl
 */
@Module.Info(
        name = "Tracers",
        description = "Draws lines to other living entities",
        category = Module.Category.RENDER
)
public class Tracers extends Module {

    private Setting<Boolean> players = register(Settings.b("Players", true));
    private Setting<Boolean> friends = register(Settings.b("Friends", true));
    private Setting<Boolean> animals = register(Settings.b("Animals", false));
    private Setting<Boolean> mobs = register(Settings.b("Mobs", false));
    private Setting<Double> range = register(Settings.d("Range", 200));
    private Setting<Boolean> renderInvis = register(Settings.b("Invisible", false));
    private Setting<Boolean> customColours = register(Settings.booleanBuilder("Custom Colours").withValue(true).build());
    private Setting<Float> opacity = register(Settings.floatBuilder("Opacity").withRange(0f, 1f).withValue(1f).build());
    private Setting<Integer> r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility(v -> customColours.getValue()).build());
    private Setting<Integer> g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility(v -> customColours.getValue()).build());
    private Setting<Integer> b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility(v -> customColours.getValue()).build());

    HueCycler cycler = new HueCycler(3600);

    @Override
    public void onWorldRender(RenderEvent event) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().world.loadedEntityList.stream()
                .filter(EntityUtil::isLiving)
                .filter(entity -> {
                    if (entity.isInvisible()) {
                        return renderInvis.getValue();
                    }
                    return true;
                })
                .filter(entity -> !EntityUtil.isFakeLocalPlayer(entity))
                .filter(entity -> (entity instanceof EntityPlayer ? players.getValue() && mc.player != entity : (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue())))
                .filter(entity -> mc.player.getDistance(entity) < range.getValue())
                .forEach(entity -> {
                    int colour = getColour(entity);
                    if (colour == ColourUtils.Colors.RAINBOW) {
                        if (!friends.getValue()) return;
                        if (customColours.getValue()) {
                            colour = rgbToInt(r.getValue(), g.getValue(), b.getValue(), (int) (opacity.getValue() * 255f));
                        } else {
                            colour = cycler.current();
                        }
                    } else {
                        colour = cycler.current();
                    }
                    final float r = ((colour >>> 16) & 0xFF) / 255f;
                    final float g = ((colour >>> 8) & 0xFF) / 255f;
                    final float b = (colour & 0xFF) / 255f;
                    drawLineToEntity(entity, r, g, b, opacity.getValue());
                });
        GlStateManager.popMatrix();
    }

    @Override
    public void onUpdate() {
        cycler.next();
    }

    private void drawRainbowToEntity(Entity entity, float opacity) {
        Vec3d eyes = new Vec3d(0, 0, 1)
                .rotatePitch(-(float) Math
                        .toRadians(Minecraft.getMinecraft().player.rotationPitch))
                .rotateYaw(-(float) Math
                        .toRadians(Minecraft.getMinecraft().player.rotationYaw));
        double[] xyz = interpolate(entity);
        double posx = xyz[0];
        double posy = xyz[1];
        double posz = xyz[2];
        double posx2 = eyes.x;
        double posy2 = eyes.y + mc.player.getEyeHeight();
        double posz2 = eyes.z;

        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        cycler.reset();
        cycler.setNext(opacity);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();
        mc.entityRenderer.orientCamera(mc.getRenderPartialTicks());

        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex3d(posx, posy, posz);
            GL11.glVertex3d(posx2, posy2, posz2);
            cycler.setNext(opacity);
            GL11.glVertex3d(posx2, posy2, posz2);
            GL11.glVertex3d(posx2, posy2, posz2);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3d(1d, 1d, 1d);
        GlStateManager.enableLighting();
    }

    private int getColour(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return Friends.isFriend(entity.getName()) ? ColourUtils.Colors.RAINBOW : ColourUtils.Colors.WHITE;
        } else {
            if (EntityUtil.isPassive(entity)) return ColourUtils.Colors.GREEN;
            else
                return ColourUtils.Colors.RED;
        }
    }

    public static double interpolate(double now, double then) {
        return then + (now - then) * mc.getRenderPartialTicks();
    }

    public static double[] interpolate(Entity entity) {
        double posX = interpolate(entity.posX, entity.lastTickPosX) - mc.getRenderManager().renderPosX;
        double posY = interpolate(entity.posY, entity.lastTickPosY) - mc.getRenderManager().renderPosY;
        double posZ = interpolate(entity.posZ, entity.lastTickPosZ) - mc.getRenderManager().renderPosZ;
        return new double[]{posX, posY, posZ};
    }

    public static void drawLineToEntity(Entity e, float red, float green, float blue, float opacity) {
        double[] xyz = interpolate(e);
        drawLine(xyz[0], xyz[1], xyz[2], e.height, red, green, blue, opacity);
    }

    public static void drawLine(double posx, double posy, double posz, double up, float red, float green, float blue, float opacity) {
        Vec3d eyes = new Vec3d(0, 0, 1)
                .rotatePitch(-(float) Math
                        .toRadians(Minecraft.getMinecraft().player.rotationPitch))
                .rotateYaw(-(float) Math
                        .toRadians(Minecraft.getMinecraft().player.rotationYaw));

        drawLineFromPosToPos(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z, posx, posy, posz, up, red, green, blue, opacity);
    }

    public static void drawLineFromPosToPos(double posx, double posy, double posz, double posx2, double posy2, double posz2, double up, float red, float green, float blue, float opacity) {
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, opacity);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();
        mc.entityRenderer.orientCamera(mc.getRenderPartialTicks());

        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex3d(posx, posy, posz);
            GL11.glVertex3d(posx2, posy2, posz2);
            GL11.glVertex3d(posx2, posy2, posz2);
            GL11.glVertex3d(posx2, posy2 + up, posz2);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glColor3d(1d, 1d, 1d);
        GlStateManager.enableLighting();
    }
}
