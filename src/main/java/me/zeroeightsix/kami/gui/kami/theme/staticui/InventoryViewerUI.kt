package me.zeroeightsix.kami.gui.kami.theme.staticui

import me.zeroeightsix.kami.gui.kami.component.InventoryViewerComponent
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI
import me.zeroeightsix.kami.module.modules.client.InventoryViewer

/**
 * Created by 086 on 11/08/2017.
 */
class InventoryViewerUI : AbstractComponentUI<InventoryViewerComponent>() {

    override fun handleSizeComponent(component: InventoryViewerComponent) {
        component.width = 162
        component.height = 54
    }

    override fun renderComponent(component: InventoryViewerComponent) {
        InventoryViewer.renderInventoryViewer()
    }
}