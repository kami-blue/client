package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.colourUtils.ColourConverter;
import me.zeroeightsix.kami.util.colourUtils.ColourHolder;
import me.zeroeightsix.kami.util.EntityUtils;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 19/12/2017.
 * Updated by snowmii on 12/12/19
 * Updated by Xiaro on 04/08/20
 */
@Module.Info(
        name = "Nametags",
        description = "Draws descriptive nametags above entities",
        category = Module.Category.RENDER
)
public class Nametags extends Module {

    private final Setting<Boolean> players = register(Settings.b("Players", true));
    private final Setting<Boolean> mobs = register(Settings.b("Mobs", false));
    private final Setting<Boolean> passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility(v -> mobs.getValue()).build());
    private final Setting<Boolean> neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility(v -> mobs.getValue()).build());
    private final Setting<Boolean> hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility(v -> mobs.getValue()).build());
    private final Setting<Double> range = register(Settings.d("Range", 200));
    private final Setting<Float> scale = register(Settings.floatBuilder("Scale").withMinimum(.5f).withMaximum(10f).withValue(2.5f).build());
    private final Setting<Boolean> health = register(Settings.b("Health", true));
    private final Setting<Boolean> armor = register(Settings.b("Armor", true));

    @Override
    public void onWorldRender(RenderEvent event) {
        if (mc.getRenderManager().options == null) return;
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Minecraft.getMinecraft().world.loadedEntityList.stream()
                .filter(EntityUtils::isLiving)
                .filter(entity -> !EntityUtils.isFakeLocalPlayer(entity))
                .filter(entity -> (entity instanceof EntityPlayer ? players.getValue() && mc.player != entity : (EntityUtils.mobTypeSettings(entity, mobs.getValue(), passive.getValue(), neutral.getValue(), hostile.getValue()))))
                .filter(entity -> mc.player.getDistance(entity) < range.getValue())
                .sorted(Comparator.comparing(entity -> -mc.player.getDistance(entity)))
                .forEach(this::drawNametag);
    }

    private void drawNametag(Entity entityIn) {
        GlStateManager.pushMatrix();

        Vec3d interp = EntityUtils.getInterpolatedRenderPos(entityIn, mc.getRenderPartialTicks());
        float yAdd = entityIn.height + 0.5F - (entityIn.isSneaking() ? 0.25F : 0.0F);
        double x = interp.x;
        double y = interp.y + yAdd;
        double z = interp.z;

        float viewerYaw = mc.getRenderManager().playerViewY;
        float viewerPitch = mc.getRenderManager().playerViewX;
        boolean isThirdPersonFrontal = mc.getRenderManager().options.thirdPersonView == 2;
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);

        float f = mc.player.getDistance(entityIn);
        float m = (f / 8f) * (float) (Math.pow(1.2589254f, this.scale.getValue()));
        GlStateManager.scale(m, m, m);

        FontRenderer fontRendererIn = mc.fontRenderer;
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);

        String str = entityIn.getName() + (health.getValue() ? " " + getHealthColoured(entityIn, Math.round(((EntityLivingBase) entityIn).getHealth() + (entityIn instanceof EntityPlayer ? ((EntityPlayer) entityIn).getAbsorptionAmount() : 0))) : "");
        int i = fontRendererIn.getStringWidth(str) / 2;
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder bufferbuilder = tessellator.getBuffer();

        glTranslatef(0, -20, 0);
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos(-i - 1, 19, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos(i + 1, 19, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        tessellator.draw();

        bufferbuilder.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-i - 1, 8, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        bufferbuilder.pos(-i - 1, 19, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        bufferbuilder.pos(i + 1, 19, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        bufferbuilder.pos(i + 1, 8, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        if (!entityIn.isSneaking())
            fontRendererIn.drawString(str, -i, 10, entityIn instanceof EntityPlayer ? Friends.isFriend(entityIn.getName()) ? 0x00bfff : 0xffffff : 0xffffff);
        else fontRendererIn.drawString(str, -i, 10, 0xffaa00);
        GlStateManager.disableTexture2D();

        if (entityIn instanceof EntityPlayer && armor.getValue())
            renderArmor((EntityPlayer) entityIn, 0, -(fontRendererIn.FONT_HEIGHT + 1) - 20);

        GlStateManager.glNormal3f(0.0F, 0.0F, 0.0F);
        glTranslatef(0, 20, 0);

        GlStateManager.scale(-40, -40, 40);
        GlStateManager.popMatrix();
    }

    public void renderArmor(EntityPlayer player, int x, int y) {
        InventoryPlayer items = player.inventory;
        ItemStack inHand = player.getHeldItemMainhand();
        ItemStack boots = items.armorItemInSlot(0);
        ItemStack leggings = items.armorItemInSlot(1);
        ItemStack body = items.armorItemInSlot(2);
        ItemStack helm = items.armorItemInSlot(3);
        ItemStack offHand = player.getHeldItemOffhand();
        ItemStack[] stuff;
        stuff = new ItemStack[]{inHand, helm, body, leggings, boots, offHand};
        ItemStack[] array = stuff;
        List<ItemStack> stacks = new ArrayList();
        for (ItemStack i : array) {
            if (i != null && i.getItem() != Items.AIR) {
                i.getItem();
                stacks.add(i);
            }
        }
        int width = 16 * stacks.size() / 2;
        x -= width;
        GlStateManager.enableTexture2D();
        for (ItemStack stack : stacks) {
            renderItem(stack, x, y);
            x += 16;
        }
        GlStateManager.disableTexture2D();
    }

    private String getHealthColoured(Entity entity, int health) {
        float max = ((EntityLivingBase) entity).getMaxHealth();
        int result = Math.round((health / max) * 100);
        if (result > 75) {
            return TextFormatting.GREEN + "" + health;
        } else if (result > 50) {
            return TextFormatting.YELLOW + "" + health;
        } else if (result > 25) {
            return TextFormatting.RED + "" + health;
        } else {
            return TextFormatting.DARK_RED + "" + health;
        }
    }

    public void renderItem(ItemStack stack, int x, int y) {
        FontRenderer fontRenderer = mc.fontRenderer;
        RenderItem renderItem = mc.getRenderItem();
        EnchantEntry[] enchants = {
                new EnchantEntry(Enchantments.PROTECTION, "Pro"),
                new EnchantEntry(Enchantments.THORNS, "Thr"),
                new EnchantEntry(Enchantments.SHARPNESS, "Sha"),
                new EnchantEntry(Enchantments.FIRE_ASPECT, "Fia"),
                new EnchantEntry(Enchantments.KNOCKBACK, "Knb"),
                new EnchantEntry(Enchantments.UNBREAKING, "Unb"),
                new EnchantEntry(Enchantments.POWER, "Pow"),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, "Fpr"),
                new EnchantEntry(Enchantments.FEATHER_FALLING, "Fea"),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, "Bla"),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, "Ppr"),
                new EnchantEntry(Enchantments.RESPIRATION, "Res"),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, "Aqu"),
                new EnchantEntry(Enchantments.DEPTH_STRIDER, "Dep"),
                new EnchantEntry(Enchantments.FROST_WALKER, "Fro"),
                new EnchantEntry(Enchantments.BINDING_CURSE, "Bin"),
                new EnchantEntry(Enchantments.SMITE, "Smi"),
                new EnchantEntry(Enchantments.BANE_OF_ARTHROPODS, "Ban"),
                new EnchantEntry(Enchantments.LOOTING, "Loo"),
                new EnchantEntry(Enchantments.SWEEPING, "Swe"),
                new EnchantEntry(Enchantments.EFFICIENCY, "Eff"),
                new EnchantEntry(Enchantments.SILK_TOUCH, "Sil"),
                new EnchantEntry(Enchantments.FORTUNE, "For"),
                new EnchantEntry(Enchantments.FLAME, "Fla"),
                new EnchantEntry(Enchantments.LUCK_OF_THE_SEA, "Luc"),
                new EnchantEntry(Enchantments.LURE, "Lur"),
                new EnchantEntry(Enchantments.MENDING, "Men"),
                new EnchantEntry(Enchantments.VANISHING_CURSE, "Van"),
                new EnchantEntry(Enchantments.PUNCH, "Pun")
        };
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - 3, y + 8, 0.0F);
        GlStateManager.scale(0.3F, 0.3F, 0.3F);
        GlStateManager.popMatrix();

        RenderHelper.enableGUIStandardItemLighting();
        renderItem.zLevel = -100.0F;
        renderItem.renderItemIntoGUI(stack, x, y);
        renderItem.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, null);
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        if (stack.isItemStackDamageable()) drawDamage(stack, x, y);
        GlStateManager.scale(1.33F, 1.33F, 1.33F);
        EnchantEntry[] array;
        int length = (array = enchants).length;
        for (int i = 0; i < length; i++) {
            EnchantEntry enchant = array[i];
            int level = EnchantmentHelper.getEnchantmentLevel(enchant.getEnchant(), stack);
            String levelDisplay = "" + level;
            if (level > 10) {
                levelDisplay = "10+";
            }
            if (level > 0) {
                float scale2 = 0.32F;
                GlStateManager.translate(x - 1, y + 2, 0.0F);
                GlStateManager.scale(0.42F, 0.42F, 0.42F);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                fontRenderer.drawString("\u00a7f" + enchant.getName() + " " + levelDisplay,
                        20 - fontRenderer.getStringWidth("\u00a7f" + enchant.getName() + " " + levelDisplay) / 2, 0, Color.WHITE.getRGB(), true);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.scale(2.42F, 2.42F, 2.42F);
                GlStateManager.translate(-x + 1, -y, 0.0F);
                y += (int) ((fontRenderer.FONT_HEIGHT + 3) * 0.28F);
            }
        }
        renderItem.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public void drawDamage(ItemStack itemstack, int x, int y) {
        float green = ((float) itemstack.getMaxDamage() - (float) itemstack.getItemDamage()) / (float) itemstack.getMaxDamage();
        float red = 1 - green;
        int dmg = 100 - (int) (red * 100);
        mc.fontRenderer.drawStringWithShadow(dmg + "", x + 8 - mc.fontRenderer.getStringWidth(dmg + "") / 2f, y - 11, ColourConverter.rgbToInt((int) (red * 255), (int) (green * 255), 0));
    }

    public static class EnchantEntry {
        private Enchantment enchant;
        private String name;

        public EnchantEntry(Enchantment enchant, String name) {
            this.enchant = enchant;
            this.name = name;
        }

        public Enchantment getEnchant() {
            return this.enchant;
        }

        public String getName() {
            return this.name;
        }
    }
}
