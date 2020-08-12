// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.dev;

import me.zeroeightsix.kami.util.FileHelper;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.tileentity.TileEntity;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.Settings;
import java.util.concurrent.Executors;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.util.math.BlockPos;
import java.util.concurrent.ScheduledExecutorService;
import me.zeroeightsix.kami.module.Module;

@Info(name = "StashFinderDev", description = "Show coords of chests and shulker boxes", category = Category.DEV)
public class StashFinderDev extends Module
{
    private ScheduledExecutorService scheduler;
    private BlockPos uwu;
    private Setting<Integer> RenderDistance;
    private int idk;
    private Setting<Boolean> SaveToFile;
    
    public StashFinderDev() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.RenderDistance = this.register(Settings.integerBuilder("RenderDistance").withMinimum(0).withMaximum(16).withValue(4));
        this.SaveToFile = this.register(Settings.b("Save To File", true));
    }
    
    @Override
    protected void onEnable() {
        this.uwu = CrystalAura.getPlayerPos();
    }
    
    @Override
    public void onWorldRender(final RenderEvent event) {
        GlStateManager.func_179094_E();
        final Iterator<TileEntity> iterator;
        TileEntity tileEntity;
        BlockPos pos;
        String owo;
        this.scheduler.scheduleAtFixedRate(() -> {
            this.idk = this.RenderDistance.getValue() * 16;
            if (CrystalAura.getPlayerPos().field_177962_a >= this.uwu.field_177962_a + this.idk || CrystalAura.getPlayerPos().field_177962_a <= this.uwu.field_177962_a - this.idk || CrystalAura.getPlayerPos().field_177961_c >= this.uwu.field_177961_c + this.idk || CrystalAura.getPlayerPos().field_177961_c <= this.uwu.field_177961_c - this.idk) {
                Wrapper.getWorld().field_147482_g.iterator();
                while (iterator.hasNext()) {
                    tileEntity = iterator.next();
                    if (!ModuleManager.isModuleEnabled("StashFinderDev")) {
                        break;
                    }
                    else {
                        pos = tileEntity.func_174877_v();
                        this.uwu = CrystalAura.getPlayerPos();
                        owo = String.valueOf(pos.field_177962_a) + " ," + String.valueOf(pos.field_177960_b) + " ," + String.valueOf(pos.field_177961_c);
                        Command.sendChatMessage("[StashFinder] Found Chest in Pos (" + owo + ")");
                        this.sendNotification("[StashFinder] Found Chest in Pos (" + owo + ")");
                    }
                }
            }
            return;
        }, 0L, 1500L, TimeUnit.MILLISECONDS);
        GlStateManager.func_179121_F();
        GlStateManager.func_179098_w();
    }
    
    private void sendNotification(final String s) {
        if (this.SaveToFile.getValue()) {
            FileHelper.appendTextFile(s, "AstraMod_coordlogger.txt");
        }
    }
}
