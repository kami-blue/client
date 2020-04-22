package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameType;

/**
 * Created by 086 on 24/01/2018.
 */
@Module.Info(
        name = "ArmourHUD",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Displays your armour and it's durability on screen"
)
public class ArmourHUD extends Module {

    private static RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

    private Setting<Boolean> damage = register(Settings.b("Damage", false));

    private NonNullList<ItemStack> getArmour() {
        if (mc.playerController.getCurrentGameType().equals(GameType.CREATIVE) || mc.playerController.getCurrentGameType().equals(GameType.SPECTATOR)) {
            return NonNullList.withSize(4, ItemStack.EMPTY);
        }
        else {
            return mc.player.inventory.armorInventory;
        }
    }

    @Override
    public void onRender() {
        GlStateManager.enableTexture2D();

        ScaledResolution resolution = new ScaledResolution(mc);
        int i = resolution.getScaledWidth() / 2;
        int iteration = 0;
        int y = resolution.getScaledHeight() - 55 - (mc.player.isInWater() ? 10 : 0);

        for (ItemStack is : getArmour()) {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();

            itemRender.zLevel = 200F;
            itemRender.renderItemAndEffectIntoGUI(is, x, y);
            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            itemRender.zLevel = 0F;

            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            mc.fontRenderer.drawStringWithShadow(s, x + 19 - 2 - mc.fontRenderer.getStringWidth(s), y + 9, 0xffffff);

            if (damage.getValue()) {
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1 - green;
                int dmg = 100 - (int) (red * 100);
                mc.fontRenderer.drawStringWithShadow(dmg + "", x + 8 - mc.fontRenderer.getStringWidth(dmg + "") / 2, y - 11, ColourHolder.toHex((int) (red * 255), (int) (green * 255), 0));
            }
        }
    GlStateManager.enableDepth();
    GlStateManager.disableLighting();
    }
}
