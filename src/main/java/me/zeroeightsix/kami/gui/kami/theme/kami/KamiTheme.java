package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.kami.theme.staticui.InventoryViewerUI;
import me.zeroeightsix.kami.gui.kami.theme.staticui.RadarUI;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.theme.AbstractTheme;

/**
 * Created by 086 on 26/06/2017.
 */
public class KamiTheme extends AbstractTheme {

    public KamiTheme() {
        installUI(new RootButtonUI<>());
        installUI(new GUI_UI());
        installUI(new RootGroupboxUI());
        installUI(new KamiFrameUI<>());
        installUI(new RootScrollpaneUI());
        installUI(new RootInputFieldUI<>());
        installUI(new RootLabelUI<>());
        installUI(new RootChatUI());
        installUI(new RootCheckButtonUI<>());
        installUI(new KamiActiveModulesUI());
        installUI(new KamiPotionUi());
        installUI(new KamiSettingsPanelUI());
        installUI(new RootSliderUI());
        installUI(new KamiEnumButtonUI());
        installUI(new RootColorizedCheckButtonUI());
        installUI(new KamiUnboundSliderUI());

        installUI(new RadarUI());
        installUI(new InventoryViewerUI());
    }

    public static class GUI_UI extends AbstractComponentUI<KamiGUI> {
    }
}
