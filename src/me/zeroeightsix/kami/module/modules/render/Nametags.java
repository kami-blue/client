// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.init.Items;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import me.zeroeightsix.kami.util.Enemies;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.Tessellator;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.entity.EntityLivingBase;
import java.text.DecimalFormat;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.RenderHelper;
import java.util.function.Consumer;
import java.util.Comparator;
import net.minecraft.entity.player.EntityPlayer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.renderer.RenderItem;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Nametags", description = "Draws descriptive nametags above entities", category = Category.RENDER)
public class Nametags extends Module
{
    private Setting<Boolean> players;
    private Setting<Boolean> animals;
    private Setting<Boolean> mobs;
    private Setting<Double> range;
    private Setting<Float> scale;
    private Setting<Boolean> health;
    private Setting<Boolean> pingSetting;
    RenderItem itemRenderer;
    private String str;
    public static Nametags instance;
    
    public Nametags() {
        this.players = this.register(Settings.b("Players", true));
        this.animals = this.register(Settings.b("Animals", false));
        this.mobs = this.register(Settings.b("Mobs", false));
        this.range = this.register(Settings.d("Range", 200.0));
        this.scale = this.register((Setting<Float>)Settings.floatBuilder("Scale").withMinimum(0.5f).withMaximum(10.0f).withValue(1.0f).build());
        this.health = this.register(Settings.b("Health", true));
        this.pingSetting = this.register(Settings.b("Ping", true));
        this.itemRenderer = Nametags.mc.func_175599_af();
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        if (Nametags.mc.func_175598_ae().field_78733_k == null) {
            return;
        }
        GlStateManager.func_179098_w();
        GlStateManager.func_179140_f();
        GlStateManager.func_179097_i();
        Minecraft.func_71410_x().field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter(entity -> !EntityUtil.isFakeLocalPlayer(entity)).filter(entity -> (entity instanceof EntityPlayer) ? (this.players.getValue() && Nametags.mc.field_71439_g != entity) : (EntityUtil.isPassive(entity) ? this.animals.getValue() : ((boolean)this.mobs.getValue()))).filter(entity -> Nametags.mc.field_71439_g.func_70032_d(entity) < this.range.getValue()).sorted(Comparator.comparing(entity -> -Nametags.mc.field_71439_g.func_70032_d(entity))).forEach(this::drawNametag);
        GlStateManager.func_179090_x();
        RenderHelper.func_74518_a();
        GlStateManager.func_179145_e();
        GlStateManager.func_179126_j();
    }
    
    public void drawNametag(final Entity entityIn) {
        GlStateManager.func_179094_E();
        final Vec3d interp = EntityUtil.getInterpolatedRenderPos(entityIn, Nametags.mc.func_184121_ak());
        final float yAdd = entityIn.field_70131_O + 0.5f - (entityIn.func_70093_af() ? 0.25f : 0.0f);
        final double x = interp.field_72450_a;
        final double y = interp.field_72448_b + yAdd;
        final double z = interp.field_72449_c;
        final float viewerYaw = Nametags.mc.func_175598_ae().field_78735_i;
        final float viewerPitch = Nametags.mc.func_175598_ae().field_78732_j;
        final boolean isThirdPersonFrontal = Nametags.mc.func_175598_ae().field_78733_k.field_74320_O == 2;
        GlStateManager.func_179137_b(x, y, z);
        GlStateManager.func_179114_b(-viewerYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179114_b((isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0f, 0.0f, 0.0f);
        final float f = Nametags.mc.field_71439_g.func_70032_d(entityIn);
        final float m = f / 8.0f * (float)Math.pow(1.258925437927246, this.scale.getValue());
        GlStateManager.func_179152_a(m, m, m);
        final FontRenderer fontRendererIn = Nametags.mc.field_71466_p;
        GlStateManager.func_179152_a(-0.025f, -0.025f, 0.025f);
        final DecimalFormat df = new DecimalFormat("##");
        String ping = null;
        try {
            ping = df.format(clamp((float)Nametags.mc.func_147114_u().func_175102_a(entityIn.func_110124_au()).func_178853_c(), 0.0f, 1000.0f));
        }
        catch (NullPointerException ex) {}
        final float healthValue = (float)Math.round(((EntityLivingBase)entityIn).func_110143_aJ());
        final float goldHearts = ((EntityPlayer)entityIn).func_110139_bj();
        final float totalHealth = healthValue + goldHearts;
        if (totalHealth <= 36.0f && totalHealth >= 21.0f) {
            this.str = (this.pingSetting.getValue() ? (ping + "ms ") : "") + entityIn.func_70005_c_() + (this.health.getValue() ? (" " + Command.SECTIONSIGN() + "6" + Math.round(((EntityLivingBase)entityIn).func_110143_aJ() + ((entityIn instanceof EntityPlayer) ? ((EntityPlayer)entityIn).func_110139_bj() : 0.0f))) : "");
        }
        else if (totalHealth <= 20.0f && totalHealth >= 15.0f) {
            this.str = (this.pingSetting.getValue() ? (ping + "ms ") : "") + entityIn.func_70005_c_() + (this.health.getValue() ? (" " + Command.SECTIONSIGN() + "a" + Math.round(((EntityLivingBase)entityIn).func_110143_aJ() + ((entityIn instanceof EntityPlayer) ? ((EntityPlayer)entityIn).func_110139_bj() : 0.0f))) : "");
        }
        else if (totalHealth <= 14.0f && totalHealth >= 8.0f) {
            this.str = (this.pingSetting.getValue() ? (ping + "ms ") : "") + entityIn.func_70005_c_() + (this.health.getValue() ? (" " + Command.SECTIONSIGN() + "e" + Math.round(((EntityLivingBase)entityIn).func_110143_aJ() + ((entityIn instanceof EntityPlayer) ? ((EntityPlayer)entityIn).func_110139_bj() : 0.0f))) : "");
        }
        else if (totalHealth <= 7.0f && totalHealth >= 4.0f) {
            this.str = (this.pingSetting.getValue() ? (ping + "ms ") : "") + entityIn.func_70005_c_() + (this.health.getValue() ? (" " + Command.SECTIONSIGN() + "c" + Math.round(((EntityLivingBase)entityIn).func_110143_aJ() + ((entityIn instanceof EntityPlayer) ? ((EntityPlayer)entityIn).func_110139_bj() : 0.0f))) : "");
        }
        else if (totalHealth <= 3.0f && totalHealth >= 0.0f) {
            this.str = (this.pingSetting.getValue() ? (ping + "ms ") : "") + entityIn.func_70005_c_() + (this.health.getValue() ? (" " + Command.SECTIONSIGN() + "4" + Math.round(((EntityLivingBase)entityIn).func_110143_aJ() + ((entityIn instanceof EntityPlayer) ? ((EntityPlayer)entityIn).func_110139_bj() : 0.0f))) : "");
        }
        final int i = fontRendererIn.func_78256_a(this.str) / 2;
        GlStateManager.func_179147_l();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_179090_x();
        final Tessellator tessellator = Tessellator.func_178181_a();
        final BufferBuilder bufferbuilder = tessellator.func_178180_c();
        GL11.glTranslatef(0.0f, -20.0f, 0.0f);
        bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        bufferbuilder.func_181662_b((double)(-i - 1), 8.0, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.5f).func_181675_d();
        bufferbuilder.func_181662_b((double)(-i - 1), 19.0, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.5f).func_181675_d();
        bufferbuilder.func_181662_b((double)(i + 1), 19.0, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.5f).func_181675_d();
        bufferbuilder.func_181662_b((double)(i + 1), 8.0, 0.0).func_181666_a(0.0f, 0.0f, 0.0f, 0.5f).func_181675_d();
        tessellator.func_78381_a();
        bufferbuilder.func_181668_a(2, DefaultVertexFormats.field_181706_f);
        bufferbuilder.func_181662_b((double)(-i - 1), 8.0, 0.0).func_181666_a(0.1f, 0.1f, 0.1f, 0.1f).func_181675_d();
        bufferbuilder.func_181662_b((double)(-i - 1), 19.0, 0.0).func_181666_a(0.1f, 0.1f, 0.1f, 0.1f).func_181675_d();
        bufferbuilder.func_181662_b((double)(i + 1), 19.0, 0.0).func_181666_a(0.1f, 0.1f, 0.1f, 0.1f).func_181675_d();
        bufferbuilder.func_181662_b((double)(i + 1), 8.0, 0.0).func_181666_a(0.1f, 0.1f, 0.1f, 0.1f).func_181675_d();
        tessellator.func_78381_a();
        GlStateManager.func_179098_w();
        GlStateManager.func_187432_a(0.0f, 1.0f, 0.0f);
        if (entityIn instanceof EntityPlayer) {
            if (Friends.isFriend(entityIn.func_70005_c_())) {
                fontRendererIn.func_78276_b(this.str, -i, 10, 1175057);
            }
            else if (Enemies.isEnemy(entityIn.func_70005_c_())) {
                fontRendererIn.func_78276_b(this.str, -i, 10, 15601937);
            }
            else {
                fontRendererIn.func_78276_b(this.str, -i, 10, 16777215);
            }
        }
        GlStateManager.func_187432_a(0.0f, 0.0f, 0.0f);
        GL11.glTranslatef(0.0f, 20.0f, 0.0f);
        GlStateManager.func_179152_a(-40.0f, -40.0f, 40.0f);
        final ArrayList<ItemStack> equipment = new ArrayList<ItemStack>();
        final ArrayList<ItemStack> list;
        entityIn.func_184214_aD().forEach(itemStack -> {
            if (itemStack != null) {
                list.add(itemStack);
            }
            return;
        });
        final ArrayList<ItemStack> armour = new ArrayList<ItemStack>();
        final ArrayList<ItemStack> list2;
        entityIn.func_184193_aE().forEach(itemStack -> {
            if (itemStack != null) {
                list2.add(itemStack);
            }
            return;
        });
        Collections.reverse(armour);
        equipment.addAll(armour);
        if (equipment.size() == 0) {
            GlStateManager.func_179121_F();
            return;
        }
        final Collection<ItemStack> a = equipment.stream().filter(itemStack -> !itemStack.func_190926_b()).collect((Collector<? super Object, ?, Collection<ItemStack>>)Collectors.toList());
        GlStateManager.func_179137_b((double)((a.size() - 1) / 2.0f * 0.5f), 0.6, 0.0);
        final int posX = (int)((a.size() - 1) / 2.0f * 0.5f);
        a.forEach(itemStack -> {
            GlStateManager.func_179123_a();
            RenderHelper.func_74519_b();
            GlStateManager.func_179139_a(0.5, 0.5, 0.0);
            GlStateManager.func_179140_f();
            this.itemRenderer.field_77023_b = -5.0f;
            this.itemRenderer.func_181564_a(itemStack, (itemStack.func_77973_b() == Items.field_185159_cQ) ? ItemCameraTransforms.TransformType.FIXED : ItemCameraTransforms.TransformType.NONE);
            GlStateManager.func_179152_a(2.0f, 2.0f, this.itemRenderer.field_77023_b = 0.0f);
            GlStateManager.func_179099_b();
            GlStateManager.func_179109_b(-0.5f, 0.0f, 0.0f);
            return;
        });
        GlStateManager.func_179121_F();
    }
    
    public static float clamp(float val, final float min, final float max) {
        if (val <= min) {
            val = min;
        }
        if (val >= max) {
            val = max;
        }
        return val;
    }
    
    static {
        Nametags.instance = new Nametags();
    }
}
