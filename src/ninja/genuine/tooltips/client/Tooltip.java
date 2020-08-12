// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client;

import net.minecraftforge.fml.common.Loader;
import net.minecraft.entity.Entity;
import ninja.genuine.utils.ModUtils;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.util.ITooltipFlag;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.client.gui.ScaledResolution;
import ninja.genuine.tooltips.client.config.Config;
import net.minecraft.client.Minecraft;

public class Tooltip implements Comparable<Tooltip>
{
    private static final Minecraft mc;
    private static final Config cfg;
    private ScaledResolution sr;
    private EntityItem entity;
    private EntityPlayer player;
    private TextFormatting textFormatting;
    private List<String> text;
    private int width;
    private int height;
    private int tickCount;
    private int fadeCount;
    public double distanceToPlayer;
    public double scale;
    public int alpha;
    public int colorBackground;
    public int colorOutline;
    public int colorOutlineShade;
    private boolean forceFade;
    private boolean countDown;
    
    public Tooltip(final EntityPlayer player, final EntityItem entity) {
        this.sr = new ScaledResolution(Tooltip.mc);
        this.text = new ArrayList<String>();
        this.countDown = true;
        this.player = player;
        this.entity = entity;
        this.textFormatting = entity.func_92059_d().func_77953_t().field_77937_e;
        this.generateTooltip(player);
        this.calculateSize();
        this.fadeCount = Tooltip.cfg.getFadeTime();
        this.tickCount = Tooltip.cfg.getShowTime() + this.fadeCount;
    }
    
    private void generateTooltip(final EntityPlayer player) {
        final boolean advanced = Tooltip.mc.field_71474_y.field_82882_x;
        this.text = (List<String>)this.entity.func_92059_d().func_82840_a(player, (ITooltipFlag)(advanced ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL));
        if (!this.modsAreLoaded() && !Tooltip.cfg.isHidingModName()) {
            this.text.add(ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC.toString() + ModUtils.getModName(this.entity) + ChatFormatting.RESET.toString());
        }
        if (this.entity.func_92059_d().func_190916_E() > 1) {
            this.text.set(0, this.entity.func_92059_d().func_190916_E() + " x " + this.text.get(0));
        }
    }
    
    private void calculateSize() {
        int max = 0;
        for (int line = 0; line < this.text.size(); ++line) {
            final int tmp = Tooltip.mc.field_71466_p.func_78256_a((String)this.text.get(line));
            if (tmp > max) {
                max = tmp;
            }
        }
        this.width = max;
        this.height = 8;
        if (this.size() > 1) {
            this.height += 2 + (this.size() - 1) * 10;
        }
    }
    
    public void tick() {
        this.sr = new ScaledResolution(Tooltip.mc);
        if (this.entity == null || this.entity.field_70128_L) {
            this.tickCount = 0;
        }
        if (this.countDown) {
            --this.tickCount;
        }
        else {
            this.tickCount += Tooltip.cfg.getFadeTime() / 4;
        }
        if (this.tickCount < 0) {
            this.tickCount = 0;
        }
        if (this.tickCount > Tooltip.cfg.getShowTime() + this.fadeCount) {
            this.tickCount = Tooltip.cfg.getShowTime() + this.fadeCount;
        }
        this.generateTooltip(this.player);
        this.calculateSize();
        this.distanceToPlayer = this.entity.func_70032_d((Entity)this.player);
        this.scale = this.distanceToPlayer / ((6 - this.sr.func_78325_e()) * 160);
        if (this.scale < 0.01) {
            this.scale = 0.01;
        }
        this.scale *= Tooltip.cfg.getScale().getDouble();
        if (this.getFade() > Tooltip.cfg.getOpacity().getDouble()) {
            this.alpha = ((int)(Tooltip.cfg.getOpacity().getDouble() * 255.0) & 0xFF) << 24;
        }
        else {
            this.alpha = ((int)(this.getFade() * 255.0) & 0xFF) << 24;
        }
        this.colorBackground = (Tooltip.cfg.getBackgroundColor() | this.alpha);
        this.colorOutline = (((Tooltip.cfg.isOverridingOutline() ? Tooltip.cfg.getOutlineColor() : ModUtils.getRarityColor(this)) | this.alpha) & 0xFFE0E0E0);
        this.colorOutlineShade = ((this.colorOutline & 0xFEFEFE) >> 1 | this.alpha);
        this.countDown = true;
    }
    
    public double getFade() {
        if (this.tickCount > this.fadeCount) {
            return 1.0;
        }
        return Math.abs(Math.pow(-1.0, 2.0) * (this.tickCount / (double)this.fadeCount));
    }
    
    public void forceFade() {
        if (this.forceFade) {
            return;
        }
        this.tickCount = 10;
        this.fadeCount = 10;
        this.forceFade = true;
    }
    
    private boolean modsAreLoaded() {
        return Loader.isModLoaded("waila") | Loader.isModLoaded("nei") | Loader.isModLoaded("hwyla");
    }
    
    @Override
    public int compareTo(final Tooltip o) {
        return (int)(o.distanceToPlayer * 10000.0 - this.distanceToPlayer * 10000.0);
    }
    
    public boolean reset() {
        if (this.forceFade) {
            return false;
        }
        this.countDown = false;
        return true;
    }
    
    public EntityItem getEntity() {
        return this.entity;
    }
    
    public int getTickCount() {
        return this.tickCount;
    }
    
    public boolean isDead() {
        return this.tickCount <= 0;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public int size() {
        return this.text.size();
    }
    
    public List<String> getText() {
        return this.text;
    }
    
    public TextFormatting formattingColor() {
        return this.textFormatting;
    }
    
    static {
        mc = Minecraft.func_71410_x();
        cfg = Config.getInstance();
    }
}
