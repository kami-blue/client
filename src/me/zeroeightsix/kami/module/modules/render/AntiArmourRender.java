// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.inventory.EntityEquipmentSlot;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AntiArmourRender", category = Category.RENDER)
public class AntiArmourRender extends Module
{
    public Setting<Boolean> player;
    public Setting<Boolean> armourstand;
    public Setting<Boolean> mobs;
    public Setting<Boolean> helmet;
    public Setting<Boolean> chestplate;
    public Setting<Boolean> leggins;
    public Setting<Boolean> boots;
    public static AntiArmourRender INSTANCE;
    
    public AntiArmourRender() {
        this.player = this.register(Settings.b("Render On Players", true));
        this.armourstand = this.register(Settings.b("Render On Armour Stands", true));
        this.mobs = this.register(Settings.b("Render On Mobs", true));
        this.helmet = this.register(Settings.b("Render Helmet", true));
        this.chestplate = this.register(Settings.b("Render Chestplate", true));
        this.leggins = this.register(Settings.b("Render Leggings", true));
        this.boots = this.register(Settings.b("Render Boots", true));
        AntiArmourRender.INSTANCE = this;
    }
    
    public static boolean shouldRenderPiece(final EntityEquipmentSlot slotIn) {
        return (slotIn == EntityEquipmentSlot.HEAD && AntiArmourRender.INSTANCE.helmet.getValue()) || (slotIn == EntityEquipmentSlot.CHEST && AntiArmourRender.INSTANCE.chestplate.getValue()) || (slotIn == EntityEquipmentSlot.LEGS && AntiArmourRender.INSTANCE.leggins.getValue()) || (slotIn == EntityEquipmentSlot.FEET && AntiArmourRender.INSTANCE.boots.getValue());
    }
}
