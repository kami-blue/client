// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.utils;

import java.util.HashMap;
import java.util.ConcurrentModificationException;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.util.Optional;
import org.apache.commons.lang3.text.WordUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.item.EntityItem;
import ninja.genuine.tooltips.client.Tooltip;
import ninja.genuine.tooltips.client.config.Config;
import java.util.Iterator;
import net.minecraft.client.gui.FontRenderer;
import java.util.Locale;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import java.util.Map;

public class ModUtils
{
    private static final Map<String, String> itemId_modName;
    private static final Map<TextFormatting, Integer> formatting_color;
    
    public static void post() {
        final FontRenderer fr = Minecraft.func_71410_x().field_71466_p;
        for (final TextFormatting color : TextFormatting.values()) {
            ModUtils.formatting_color.put(color, fr.func_175064_b(color.toString().replace("§", "").charAt(0)));
        }
        final Map<String, ModContainer> modMap = (Map<String, ModContainer>)Loader.instance().getIndexedModList();
        for (final Map.Entry<String, ModContainer> modEntry : modMap.entrySet()) {
            final String lowercaseId = modEntry.getKey().toLowerCase(Locale.ENGLISH);
            final String modName = modEntry.getValue().getName();
            ModUtils.itemId_modName.put(lowercaseId, modName);
        }
    }
    
    public static int getRarityColor(final TextFormatting format) {
        return ModUtils.formatting_color.getOrDefault(format, Config.getInstance().getOutlineColor());
    }
    
    public static int getRarityColor(final Tooltip tooltip) {
        return ModUtils.formatting_color.getOrDefault(tooltip.formattingColor(), Config.getInstance().getOutlineColor());
    }
    
    public static String getModName(final Tooltip tooltip) {
        return getModName(tooltip.getEntity());
    }
    
    public static String getModName(final EntityItem entity) {
        return getModName(entity.func_92059_d());
    }
    
    public static String getModName(final ItemStack stack) {
        return getModName(stack.func_77973_b());
    }
    
    public static String getModName(final Item item) {
        final ResourceLocation resource = (ResourceLocation)Item.field_150901_e.func_177774_c((Object)item);
        if (resource == null) {
            return "";
        }
        final String modId = resource.func_110624_b();
        final String modIdLC = modId.toLowerCase(Locale.ENGLISH);
        String modName = ModUtils.itemId_modName.get(modIdLC);
        if (modName == null) {
            modName = WordUtils.capitalize(modId);
            ModUtils.itemId_modName.put(modIdLC, modName);
        }
        return modName;
    }
    
    public static Optional<EntityItem> getMouseOver() {
        return getMouseOver((World)Minecraft.func_71410_x().field_71441_e, (Entity)Minecraft.func_71410_x().field_71439_g, 0.0f);
    }
    
    public static Optional<EntityItem> getMouseOver(final World world, final Entity player, final float partialTicks) throws ConcurrentModificationException {
        if (world == null || player == null) {
            return Optional.empty();
        }
        final Entity viewer = player;
        final int range = Config.getInstance().getRenderDistance();
        final Vec3d eyes = viewer.func_174824_e(partialTicks);
        final Vec3d look = viewer.func_70676_i(partialTicks);
        final Vec3d view = eyes.func_72441_c(look.field_72450_a * range, look.field_72448_b * range, look.field_72449_c * range);
        double distance = 0.0;
        EntityItem out = null;
        final List<EntityItem> list = (List<EntityItem>)world.func_72872_a((Class)EntityItem.class, viewer.func_174813_aQ().func_72321_a(look.field_72450_a * range, look.field_72448_b * range, look.field_72449_c * range).func_72314_b(1.0, 1.0, 1.0));
        for (int i = 0; i < list.size(); ++i) {
            final EntityItem entity = list.get(i);
            final AxisAlignedBB aabb = entity.func_174813_aQ().func_72317_d(0.0, 0.25, 0.0).func_186662_g(entity.func_70111_Y() + 0.1);
            final RayTraceResult ray = aabb.func_72327_a(eyes, view);
            if (aabb.func_72318_a(eyes)) {
                if (distance > 0.0) {
                    out = entity;
                    distance = 0.0;
                }
            }
            else if (ray != null) {
                final double d = eyes.func_72438_d(ray.field_72307_f);
                if (d < distance || distance == 0.0) {
                    out = entity;
                    distance = d;
                }
            }
        }
        return (out == null) ? Optional.empty() : Optional.of(out);
    }
    
    static {
        itemId_modName = new HashMap<String, String>();
        formatting_color = new HashMap<TextFormatting, Integer>();
    }
}
