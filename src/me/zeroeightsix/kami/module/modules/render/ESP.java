// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import java.util.Iterator;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import java.util.function.Predicate;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ESP", category = Category.RENDER)
public class ESP extends Module
{
    private Setting<ESPMode> mode;
    private Setting<Boolean> players;
    private Setting<Boolean> animals;
    private Setting<Boolean> mobs;
    
    public ESP() {
        this.mode = this.register(Settings.e("Mode", ESPMode.RECTANGLE));
        this.players = this.register(Settings.b("Players", true));
        this.animals = this.register(Settings.b("Animals", false));
        this.mobs = this.register(Settings.b("Mobs", false));
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (Wrapper.getMinecraft().func_175598_ae().field_78733_k == null) {
            return;
        }
        switch (this.mode.getValue()) {
            case RECTANGLE: {
                final boolean isThirdPersonFrontal = Wrapper.getMinecraft().func_175598_ae().field_78733_k.field_74320_O == 2;
                final float viewerYaw = Wrapper.getMinecraft().func_175598_ae().field_78735_i;
                final boolean b;
                final Vec3d pos;
                final float n;
                final boolean b2;
                ESP.mc.field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter(entity -> ESP.mc.field_71439_g != entity).map(entity -> entity).filter(entityLivingBase -> !entityLivingBase.field_70128_L).filter(entity -> {
                    if (!this.players.getValue() || !(entity instanceof EntityPlayer)) {
                        if (!(EntityUtil.isPassive(entity) ? this.animals.getValue() : this.mobs.getValue())) {
                            return b;
                        }
                    }
                    return b;
                }).forEach(e -> {
                    GlStateManager.func_179094_E();
                    pos = EntityUtil.getInterpolatedPos((Entity)e, event.getPartialTicks());
                    GlStateManager.func_179137_b(pos.field_72450_a - ESP.mc.func_175598_ae().field_78725_b, pos.field_72448_b - ESP.mc.func_175598_ae().field_78726_c, pos.field_72449_c - ESP.mc.func_175598_ae().field_78723_d);
                    GlStateManager.func_187432_a(0.0f, 1.0f, 0.0f);
                    GlStateManager.func_179114_b(-n, 0.0f, 1.0f, 0.0f);
                    GlStateManager.func_179114_b((float)(b2 ? -1 : 1), 1.0f, 0.0f, 0.0f);
                    GlStateManager.func_179140_f();
                    GlStateManager.func_179132_a(false);
                    GlStateManager.func_179097_i();
                    GlStateManager.func_179147_l();
                    GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    if (e instanceof EntityPlayer) {
                        GL11.glColor3f(1.0f, 1.0f, 1.0f);
                    }
                    else if (EntityUtil.isPassive((Entity)e)) {
                        GL11.glColor3f(0.11f, 0.9f, 0.11f);
                    }
                    else {
                        GL11.glColor3f(0.9f, 0.1f, 0.1f);
                    }
                    GlStateManager.func_179090_x();
                    GL11.glLineWidth(2.0f);
                    GL11.glEnable(2848);
                    GL11.glBegin(2);
                    GL11.glVertex2d((double)(-e.field_70130_N / 2.0f), 0.0);
                    GL11.glVertex2d((double)(-e.field_70130_N / 2.0f), (double)e.field_70131_O);
                    GL11.glVertex2d((double)(e.field_70130_N / 2.0f), (double)e.field_70131_O);
                    GL11.glVertex2d((double)(e.field_70130_N / 2.0f), 0.0);
                    GL11.glEnd();
                    GlStateManager.func_179121_F();
                    return;
                });
                GlStateManager.func_179126_j();
                GlStateManager.func_179132_a(true);
                GlStateManager.func_179090_x();
                GlStateManager.func_179147_l();
                GlStateManager.func_179118_c();
                GlStateManager.func_179120_a(770, 771, 1, 0);
                GlStateManager.func_179103_j(7425);
                GlStateManager.func_179097_i();
                GlStateManager.func_179089_o();
                GlStateManager.func_187441_d(1.0f);
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                break;
            }
        }
    }
    
    @Override
    public void onUpdate() {
        if (this.mode.getValue().equals(ESPMode.GLOW)) {
            for (final Entity e : ESP.mc.field_71441_e.field_72996_f) {
                if (e == null || e.field_70128_L) {
                    return;
                }
                if (e instanceof EntityPlayer && this.players.getValue() && !e.func_184202_aL()) {
                    e.func_184195_f(true);
                }
                else if (e instanceof EntityPlayer && !this.players.getValue() && e.func_184202_aL()) {
                    e.func_184195_f(false);
                }
                if (EntityUtil.isHostileMob(e) && this.mobs.getValue() && !e.func_184202_aL()) {
                    e.func_184195_f(true);
                }
                else if (EntityUtil.isHostileMob(e) && !this.mobs.getValue() && e.func_184202_aL()) {
                    e.func_184195_f(false);
                }
                if (EntityUtil.isPassive(e) && this.animals.getValue() && !e.func_184202_aL()) {
                    e.func_184195_f(true);
                }
                else {
                    if (!EntityUtil.isPassive(e) || this.animals.getValue() || !e.func_184202_aL()) {
                        continue;
                    }
                    e.func_184195_f(false);
                }
            }
        }
    }
    
    public void onDisable() {
        if (this.mode.getValue().equals(ESPMode.GLOW)) {
            for (final Entity e : ESP.mc.field_71441_e.field_72996_f) {
                e.func_184195_f(false);
            }
            ESP.mc.field_71439_g.func_184195_f(false);
        }
    }
    
    public enum ESPMode
    {
        RECTANGLE, 
        GLOW;
    }
}
