// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.Friends;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.HueCycler;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Tracers", description = "Draws lines to other living entities", category = Category.RENDER)
public class Tracers extends Module
{
    private Setting<Boolean> players;
    private Setting<Boolean> friends;
    private Setting<Boolean> animals;
    private Setting<Boolean> mobs;
    private Setting<Double> range;
    private Setting<Float> opacity;
    HueCycler cycler;
    
    public Tracers() {
        this.players = this.register(Settings.b("Players", true));
        this.friends = this.register(Settings.b("Friends", true));
        this.animals = this.register(Settings.b("Animals", false));
        this.mobs = this.register(Settings.b("Mobs", false));
        this.range = this.register(Settings.d("Range", 200.0));
        this.opacity = this.register(Settings.floatBuilder("Opacity").withRange(0.0f, 1.0f).withValue(1.0f));
        this.cycler = new HueCycler(3600);
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        GlStateManager.func_179094_E();
        int colour;
        final float r;
        final float g;
        final float b;
        Minecraft.func_71410_x().field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter(entity -> !EntityUtil.isFakeLocalPlayer(entity)).filter(entity -> (entity instanceof EntityPlayer) ? (this.players.getValue() && Tracers.mc.field_71439_g != entity) : (EntityUtil.isPassive(entity) ? this.animals.getValue() : ((boolean)this.mobs.getValue()))).filter(entity -> Tracers.mc.field_71439_g.func_70032_d(entity) < this.range.getValue()).forEach(entity -> {
            colour = this.getColour(entity);
            if (colour == Integer.MIN_VALUE) {
                if (!this.friends.getValue()) {
                    return;
                }
                else {
                    colour = this.cycler.current();
                }
            }
            r = (colour >>> 16 & 0xFF) / 255.0f;
            g = (colour >>> 8 & 0xFF) / 255.0f;
            b = (colour & 0xFF) / 255.0f;
            drawLineToEntity(entity, r, g, b, this.opacity.getValue());
            return;
        });
        GlStateManager.func_179121_F();
    }
    
    @Override
    public void onUpdate() {
        this.cycler.next();
    }
    
    private void drawRainbowToEntity(final Entity entity, final float opacity) {
        final Vec3d eyes = new Vec3d(0.0, 0.0, 1.0).func_178789_a(-(float)Math.toRadians(Minecraft.func_71410_x().field_71439_g.field_70125_A)).func_178785_b(-(float)Math.toRadians(Minecraft.func_71410_x().field_71439_g.field_70177_z));
        final double[] xyz = interpolate(entity);
        final double posx = xyz[0];
        final double posy = xyz[1];
        final double posz = xyz[2];
        final double posx2 = eyes.field_72450_a;
        final double posy2 = eyes.field_72448_b + Tracers.mc.field_71439_g.func_70047_e();
        final double posz2 = eyes.field_72449_c;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        this.cycler.reset();
        this.cycler.setNext(opacity);
        GlStateManager.func_179140_f();
        GL11.glLoadIdentity();
        Tracers.mc.field_71460_t.func_78467_g(Tracers.mc.func_184121_ak());
        GL11.glBegin(1);
        GL11.glVertex3d(posx, posy, posz);
        GL11.glVertex3d(posx2, posy2, posz2);
        this.cycler.setNext(opacity);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glColor3d(1.0, 1.0, 1.0);
        GlStateManager.func_179145_e();
    }
    
    private int getColour(final Entity entity) {
        if (entity instanceof EntityPlayer) {
            return Friends.isFriend(entity.func_70005_c_()) ? Integer.MIN_VALUE : ColourUtils.Colors.WHITE;
        }
        if (EntityUtil.isPassive(entity)) {
            return ColourUtils.Colors.GREEN;
        }
        return ColourUtils.Colors.RED;
    }
    
    public static double interpolate(final double now, final double then) {
        return then + (now - then) * Tracers.mc.func_184121_ak();
    }
    
    public static double[] interpolate(final Entity entity) {
        final double posX = interpolate(entity.field_70165_t, entity.field_70142_S) - Tracers.mc.func_175598_ae().field_78725_b;
        final double posY = interpolate(entity.field_70163_u, entity.field_70137_T) - Tracers.mc.func_175598_ae().field_78726_c;
        final double posZ = interpolate(entity.field_70161_v, entity.field_70136_U) - Tracers.mc.func_175598_ae().field_78723_d;
        return new double[] { posX, posY, posZ };
    }
    
    public static void drawLineToEntity(final Entity e, final float red, final float green, final float blue, final float opacity) {
        final double[] xyz = interpolate(e);
        drawLine(xyz[0], xyz[1], xyz[2], e.field_70131_O, red, green, blue, opacity);
    }
    
    public static void drawLine(final double posx, final double posy, final double posz, final double up, final float red, final float green, final float blue, final float opacity) {
        final Vec3d eyes = new Vec3d(0.0, 0.0, 1.0).func_178789_a(-(float)Math.toRadians(Minecraft.func_71410_x().field_71439_g.field_70125_A)).func_178785_b(-(float)Math.toRadians(Minecraft.func_71410_x().field_71439_g.field_70177_z));
        drawLineFromPosToPos(eyes.field_72450_a, eyes.field_72448_b + Tracers.mc.field_71439_g.func_70047_e(), eyes.field_72449_c, posx, posy, posz, up, red, green, blue, opacity);
    }
    
    public static void drawLineFromPosToPos(final double posx, final double posy, final double posz, final double posx2, final double posy2, final double posz2, final double up, final float red, final float green, final float blue, final float opacity) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, opacity);
        GlStateManager.func_179140_f();
        GL11.glLoadIdentity();
        Tracers.mc.field_71460_t.func_78467_g(Tracers.mc.func_184121_ak());
        GL11.glBegin(1);
        GL11.glVertex3d(posx, posy, posz);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2 + up, posz2);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glColor3d(1.0, 1.0, 1.0);
        GlStateManager.func_179145_e();
    }
}
