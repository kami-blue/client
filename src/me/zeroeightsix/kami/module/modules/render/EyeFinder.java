// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import java.util.function.Consumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import java.util.function.Predicate;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "EyeFinder", description = "Draw lines from entity's heads to where they are looking", category = Category.RENDER)
public class EyeFinder extends Module
{
    private Setting<Boolean> players;
    private Setting<Boolean> mobs;
    private Setting<Boolean> animals;
    
    public EyeFinder() {
        this.players = this.register(Settings.b("Players", true));
        this.mobs = this.register(Settings.b("Mobs", false));
        this.animals = this.register(Settings.b("Animals", false));
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        final boolean b;
        EyeFinder.mc.field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter(entity -> EyeFinder.mc.field_71439_g != entity).map(entity -> entity).filter(entityLivingBase -> !entityLivingBase.field_70128_L).filter(entity -> {
            if (!this.players.getValue() || !(entity instanceof EntityPlayer)) {
                if (!(EntityUtil.isPassive(entity) ? this.animals.getValue() : this.mobs.getValue())) {
                    return b;
                }
            }
            return b;
        }).forEach(this::drawLine);
    }
    
    private void drawLine(final EntityLivingBase e) {
        final RayTraceResult result = e.func_174822_a(6.0, Minecraft.func_71410_x().func_184121_ak());
        if (result == null) {
            return;
        }
        final Vec3d eyes = e.func_174824_e(Minecraft.func_71410_x().func_184121_ak());
        GlStateManager.func_179126_j();
        GlStateManager.func_179090_x();
        GlStateManager.func_179140_f();
        final double posx = eyes.field_72450_a - EyeFinder.mc.func_175598_ae().field_78725_b;
        final double posy = eyes.field_72448_b - EyeFinder.mc.func_175598_ae().field_78726_c;
        final double posz = eyes.field_72449_c - EyeFinder.mc.func_175598_ae().field_78723_d;
        final double posx2 = result.field_72307_f.field_72450_a - EyeFinder.mc.func_175598_ae().field_78725_b;
        final double posy2 = result.field_72307_f.field_72448_b - EyeFinder.mc.func_175598_ae().field_78726_c;
        final double posz2 = result.field_72307_f.field_72449_c - EyeFinder.mc.func_175598_ae().field_78723_d;
        GL11.glColor4f(0.2f, 0.1f, 0.3f, 0.8f);
        GlStateManager.func_187441_d(1.5f);
        GL11.glBegin(1);
        GL11.glVertex3d(posx, posy, posz);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glEnd();
        if (result.field_72313_a == RayTraceResult.Type.BLOCK) {
            KamiTessellator.prepare(7);
            GL11.glEnable(2929);
            final BlockPos b = result.func_178782_a();
            final float x = b.field_177962_a - 0.01f;
            final float y = b.field_177960_b - 0.01f;
            final float z = b.field_177961_c - 0.01f;
            KamiTessellator.drawBox(KamiTessellator.getBufferBuilder(), x, y, z, 1.01f, 1.01f, 1.01f, 51, 25, 73, 200, 63);
            KamiTessellator.release();
        }
        GlStateManager.func_179098_w();
        GlStateManager.func_179145_e();
    }
}
