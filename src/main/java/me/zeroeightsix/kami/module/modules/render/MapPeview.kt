package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemMap
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderTooltipEvent


/**
 * Created by fred41 on 27/05/2020.
 *
 */
@Module.Info(
        name = "MapPreview",
        category = Module.Category.RENDER,
        description = "Shows the contents of a map when you hover over one"
)
class MapPeview : Module() {
    private val scale = register(Settings.doubleBuilder("scale").withRange(0.0, 3.0).withValue(0.5).build())

    @EventHandler
    private val tooltipEventListener = Listener(EventHook { event: RenderTooltipEvent.PostText ->
        if (event.getStack().getItem() is ItemMap) {
            val mapdata = (event.getStack().getItem() as ItemMap).getMapData(event.getStack(), mc.world as World)
            if(mapdata==null)
                return@EventHook
            GlStateManager.disableDepth()
            GlStateManager.disableLighting()
            GlStateManager.translate(event.getX()-1.0, event.getY()+event.lines.size*15.0, 0.0)
            GlStateManager.scale(scale.value, scale.value, 1.0)
            mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, true)
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
    })
}