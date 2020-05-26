package me.zeroeightsix.kami.module.modules.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * @author Salt 5/25/2020
 */
@Module.Info(
        name = "Overlays Manager",
        description = "Choose which overlays to display",
        category = Module.Category.CLIENT
)
public class Overlays extends Module
{
    private Setting<Boolean> radarDisabled = register(Settings.b("Radar", true));
    private Setting<Boolean> baritoneDisabled = register(Settings.b("Baritone", true));
    private Setting<Boolean> activeModulesDisabled = register(Settings.b("Active Modules", true));
    private Setting<Boolean> informationOverlayDisabled = register(Settings.b("Information", true));
    private Setting<Boolean> inventoryViewerDisabled = register(Settings.b("Inventory Viewer", true));
    private Setting<Boolean> friendsListDisabled = register(Settings.b("Friends List", true));
    private Setting<Boolean> textRadarDisabled = register(Settings.b("Text Radar", true));
    private Setting<Boolean> entitylistDisabled = register(Settings.b("Entity List", true));
    private Setting<Boolean> coordinatesDisabled = register(Settings.b("Coordinates", true));

    //lastBoolean added to prevent setting visibility each update
    private boolean lastRadarDisabled = radarDisabled.getValue();
    private boolean lastBaritoneDisabled = baritoneDisabled.getValue();
    private boolean lastActiveModulesDisabled = activeModulesDisabled.getValue();
    private boolean lastInformationOverlayDisabled = informationOverlayDisabled.getValue();
    private boolean lastInventoryViewerDisabled = inventoryViewerDisabled.getValue();
    private boolean lastFriendsListDisabled = friendsListDisabled.getValue();
    private boolean lastTextRadarDisabled = textRadarDisabled.getValue();
    private boolean lastEntityListDisabled = entitylistDisabled.getValue();
    private boolean lastCoordinatesDisabled = coordinatesDisabled.getValue();

    public void onUpdate()
    {
        //get the GUI
        KamiGUI gui = KamiMod.getInstance().getGuiManager();

        //Check if there was a change in values then set the visibility according to the change
        if (radarDisabled.getValue() != lastRadarDisabled) gui.setChildVisibilityByName("RadarFrame",radarDisabled.getValue());
        if (baritoneDisabled.getValue() != lastBaritoneDisabled) gui.setChildVisibilityByName("BaritoneFrame",baritoneDisabled.getValue());
        if (activeModulesDisabled.getValue() != lastActiveModulesDisabled) gui.setChildVisibilityByName("ActiveModulesFrame",activeModulesDisabled.getValue());
        if (informationOverlayDisabled.getValue() != lastInformationOverlayDisabled) gui.setChildVisibilityByName("InformationOverlayFrame",informationOverlayDisabled.getValue());
        if (inventoryViewerDisabled.getValue() != lastInventoryViewerDisabled) gui.setChildVisibilityByName("InventoryViewerFrame",inventoryViewerDisabled.getValue());
        if (friendsListDisabled.getValue() != lastFriendsListDisabled) gui.setChildVisibilityByName("FriendsListFrame",friendsListDisabled.getValue());
        if (textRadarDisabled.getValue() != lastTextRadarDisabled) gui.setChildVisibilityByName("TextRadarFrame",textRadarDisabled.getValue());
        if (entitylistDisabled.getValue() != lastEntityListDisabled) gui.setChildVisibilityByName("EntityListFrame",entitylistDisabled.getValue());
        if (coordinatesDisabled.getValue() != lastCoordinatesDisabled) gui.setChildVisibilityByName("CoordinatesFrame",coordinatesDisabled.getValue());

        //reset the change checking variables to the new normal value
        lastRadarDisabled = radarDisabled.getValue();
        lastBaritoneDisabled = baritoneDisabled.getValue();
        lastActiveModulesDisabled = activeModulesDisabled.getValue();
        lastInformationOverlayDisabled = informationOverlayDisabled.getValue();
        lastInventoryViewerDisabled = inventoryViewerDisabled.getValue();
        lastFriendsListDisabled = friendsListDisabled.getValue();
        lastTextRadarDisabled = textRadarDisabled.getValue();
        lastEntityListDisabled = entitylistDisabled.getValue();
        lastCoordinatesDisabled = coordinatesDisabled.getValue();
    }

    public void onDisable()
    {
        KamiGUI gui = KamiMod.getInstance().getGuiManager();
        //When Overlay Manager is disabled, turn all of the overlays back on
        gui.setChildVisibilityByName("RadarFrame",true);
        gui.setChildVisibilityByName("BaritoneFrame",true);
        gui.setChildVisibilityByName("ActiveModulesFrame",true);
        gui.setChildVisibilityByName("InformationOverlayFrame",true);
        gui.setChildVisibilityByName("InventoryViewerFrame",true);
        gui.setChildVisibilityByName("FriendsListFrame",true);
        gui.setChildVisibilityByName("TextRadarFrame",true);
        gui.setChildVisibilityByName("EntityListFrame",true);
        gui.setChildVisibilityByName("CoordinatesFrame",true);

        //Change these variables so it will update when turned back on
        lastRadarDisabled = true;
        lastBaritoneDisabled = true;
        lastActiveModulesDisabled = true;
        lastInformationOverlayDisabled = true;
        lastInventoryViewerDisabled = true;
        lastFriendsListDisabled = true;
        lastTextRadarDisabled = true;
        lastEntityListDisabled = true;
        lastCoordinatesDisabled = true;
    }
}
