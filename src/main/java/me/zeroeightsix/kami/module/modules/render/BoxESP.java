package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.util.ColourConverter.rgbToInt;

/**
 * @author polymer
 * Updated by dominikaaaa on 30/03/20
 */
@Module.Info(
        name = "BoxESP",
        description = "Draws a box around small entities",
        category = Module.Category.RENDER
)
public class BoxESP extends Module {
    private Setting<Boolean> experience = register(Settings.b("Experience", true));
    private Setting<Boolean> arrows = register(Settings.b("Arrows", true));
    private Setting<Boolean> throwable = register(Settings.b("Throwable", true));
    private Setting<Boolean> items = register(Settings.b("Items", false));
    private Setting<Integer> alpha = register(Settings.integerBuilder("Alpha").withMinimum(1).withMaximum(255).withValue(100).build());
    private Setting<Integer> red = register(Settings.integerBuilder("Red").withMinimum(1).withMaximum(255).withValue(155).build());
    private Setting<Integer> green = register(Settings.integerBuilder("Green").withMinimum(1).withMaximum(255).withValue(144).build());
    private Setting<Integer> blue = register(Settings.integerBuilder("Blue").withMinimum(1).withMaximum(255).withValue(255).build());

    @Override
    public void onWorldRender(RenderEvent event) {
        List<Entity> entities = mc.world.loadedEntityList.stream().filter(this::getEntity).collect(Collectors.toList());
        for (Entity e: entities) {
            KamiTessellator.prepare(GL11.GL_QUADS);
            int colour = rgbToInt(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
            KamiTessellator.drawBoxSmall((float) e.getPositionVector().x - 0.25f, (float) e.getPositionVector().y, (float) e.getPositionVector().z - 0.25f, colour, GeometryMasks.Quad.ALL);
            KamiTessellator.release();
        }
    }

    private boolean getEntity(Entity entity) {
        if (entity instanceof EntityXPOrb && experience.getValue()) return true;
        else if (entity instanceof EntityArrow && arrows.getValue()) return true;
        else if (entity instanceof EntityThrowable && throwable.getValue()) return true;
        else return entity instanceof EntityItem && items.getValue();
    }

}
