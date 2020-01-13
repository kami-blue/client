package me.zeroeightsix.kami.module.modules.bewwawho.gui;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * Created by TBM on 30/12/2019.
 */
@Module.Info(name = "ArmourHide", category = Module.Category.GUI, description = "Hides entity armour", showOnArray = Module.ShowOnArray.OFF)
public class ArmourHide extends Module {
    public Setting<Boolean> player = register(Settings.b("Players", false));
    public Setting<Boolean> armourstand = register(Settings.b("Armour Stands", true));
    public Setting<Boolean> mobs = register(Settings.b("Mobs", true));

    public Setting<Boolean> helmet = register(Settings.b("Helmet", false));
    public Setting<Boolean> chestplate = register(Settings.b("Chestplate", false));
    public Setting<Boolean> leggins = register(Settings.b("Leggings", false));
    public Setting<Boolean> boots = register(Settings.b("Boots", false));

    public static ArmourHide INSTANCE;

    public ArmourHide() {
        ArmourHide.INSTANCE = this;
    }

    public static boolean shouldRenderPiece(EntityEquipmentSlot slotIn) {
        if (slotIn == EntityEquipmentSlot.HEAD && ArmourHide.INSTANCE.helmet.getValue()) {
            return true;
        } else if (slotIn == EntityEquipmentSlot.CHEST && ArmourHide.INSTANCE.chestplate.getValue()) {
            return true;
        } else if (slotIn == EntityEquipmentSlot.LEGS && ArmourHide.INSTANCE.leggins.getValue()) {
            return true;
        } else if (slotIn == EntityEquipmentSlot.FEET && ArmourHide.INSTANCE.boots.getValue()) {
            return true;
        } else {
            return false;
        }
    }
}