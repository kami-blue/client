package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.NecronClient
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.setting.config.Configuration
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.Macro
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.filesystem.FolderHelper
import me.zeroeightsix.kami.util.filesystem.OperatingSystemHelper
import net.minecraft.client.gui.*
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.FMLCommonHandler
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class NecronGuiKamiImportNotification : GuiScreen() {
    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(0, width / 2 - 100, 180, "Yes"))
        buttonList.add(GuiButton(1, width / 2 - 100, 205, "No"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRenderer, "NECRON Client has detected KAMI Blue configuration.", width / 2, 80, ColorConverter.rgbToHex(155, 144, 255))
        drawCenteredString(fontRenderer, "Do you want to import KAMI Blue configuration to NECRON Client?", width / 2, 110, ColorConverter.rgbToHex(255, 255, 255))
        drawCenteredString(fontRenderer, "(This will not change your KAMI Blue configuration.)", width / 2, 120, ColorConverter.rgbToHex(255, 255, 255))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            try {
                Files.copy(Paths.get("KAMIBlueConfig.json"), Paths.get(ConfigUtils.NECRON_CONFIG_NAME_DEFAULT), StandardCopyOption.REPLACE_EXISTING);
            } catch (e : Exception) { NecronClient.LOG.warn("Error when copying KAMI Blue config: " + e.cause) }
            try {
                Files.copy(Paths.get("KAMIBlueFriends.json"), Paths.get(FriendManager.configName), StandardCopyOption.REPLACE_EXISTING);
            } catch (e : Exception) { NecronClient.LOG.warn("Error when copying KAMI Blue friends: " + e.cause) }
            try {
                Files.copy(Paths.get("KAMIBlueMacros.json"), Paths.get(Macro.configName), StandardCopyOption.REPLACE_EXISTING);
            } catch (e : Exception) { NecronClient.LOG.warn("Error when copying KAMI Blue macros: " + e.cause) }
            try {
                Files.copy(Paths.get("KAMIBlueWaypoints.json"), Paths.get(WaypointManager.configName), StandardCopyOption.REPLACE_EXISTING);
            } catch (e : Exception) { NecronClient.LOG.warn("Error when copying KAMI Blue waypoints: " + e.cause) }
            ConfigUtils.loadAll()
        }
        mc.displayGuiScreen(GuiMainMenu())
    }
}