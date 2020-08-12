// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.render;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Iterator;
import java.util.Optional;
import ninja.genuine.tooltips.client.config.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import ninja.genuine.utils.ModUtils;
import java.util.function.Predicate;
import java.util.Objects;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import ninja.genuine.tooltips.client.Tooltip;
import java.util.LinkedList;

public class TooltipEvent
{
    private LinkedList<Tooltip> tooltips;
    
    public TooltipEvent() {
        this.tooltips = new LinkedList<Tooltip>();
    }
    
    @SubscribeEvent
    public void tick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        this.tooltips.removeIf(Objects::isNull);
        this.tooltips.removeIf(Tooltip::isDead);
        this.tooltips.forEach(Tooltip::tick);
        final Optional<EntityItem> mouseOver = ModUtils.getMouseOver();
        if (mouseOver.isPresent()) {
            boolean createTooltip = true;
            final EntityItem entity = mouseOver.get();
            for (final Tooltip tooltip : this.tooltips) {
                if (tooltip.getEntity() == entity) {
                    createTooltip = !tooltip.reset();
                }
            }
            if (createTooltip) {
                this.tooltips.addFirst(new Tooltip((EntityPlayer)Minecraft.func_71410_x().field_71439_g, entity));
            }
        }
        for (int i = Config.getInstance().getMaxTooltips(); i < this.tooltips.size(); ++i) {
            this.tooltips.get(i).forceFade();
        }
    }
    
    @SubscribeEvent
    public void render(final RenderWorldLastEvent event) {
        if (!Config.getInstance().isEnabled() || Minecraft.func_71410_x().field_71441_e == null) {
            return;
        }
        for (final Tooltip tooltip : this.tooltips) {
            RenderHelper.renderTooltip(tooltip, event.getPartialTicks());
        }
    }
}
